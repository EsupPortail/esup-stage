package org.esup_portail.esup_stage.service.Structure.utils;

import org.esup_portail.esup_stage.dto.LineErrorDto;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.*;
import java.util.function.Function;

@Component
public class CsvStructureImportUtils {

    @Autowired
    private NafN5JpaRepository nafN5JpaRepository;

    @Autowired
    private EffectifJpaRepository effectifJpaRepository;

    @Autowired
    private StatutJuridiqueJpaRepository statutJuridiqueJpaRepository;

    @Autowired
    private TypeStructureJpaRepository typeStructureJpaRepository;

    @Autowired
    private PaysJpaRepository paysJpaRepository;

    private final List<String> requiredHeaders = List.of(
            "NumeroRNE","RaisonSociale","NumeroSiret","ActivitePrincipale",
            "CodeAPE","Voie","CodePostal","Commune","Telephone","Fax","SiteWeb","Mail",
            "TypeStructure","StatutJuridique","Effectif","Pays"
    );

    /** Indices mappés depuis l’entête */
    public static final class Indices {
        public final int numeroRNE, raisonSociale, numeroSiret, activitePrincipale, codeApe, voie, codePostal, commune, telephone, fax, siteWeb, mail;
        public final Integer typeStructure, statutJuridique, effectif, pays;

        public Indices(Map<String, Integer> idx) {
            this.numeroRNE          = idx.get("NumeroRNE");
            this.raisonSociale      = idx.get("RaisonSociale");
            this.numeroSiret        = idx.get("NumeroSiret");
            this.activitePrincipale = idx.get("ActivitePrincipale");
            this.codeApe            = idx.get("CodeAPE");
            this.voie               = idx.get("Voie");
            this.codePostal         = idx.get("CodePostal");
            this.commune            = idx.get("Commune");
            this.telephone          = idx.get("Telephone");
            this.fax                = idx.get("Fax");
            this.siteWeb            = idx.get("SiteWeb");
            this.mail               = idx.get("Mail");
            this.typeStructure      = idx.get("TypeStructure");
            this.statutJuridique    = idx.get("StatutJuridique");
            this.effectif           = idx.get("Effectif");
            this.pays               = idx.get("Pays");
        }
    }

    /** Mappe les indices à partir de la ligne d’entête */
    public Indices mapHeaderIndices(String headerLine, String separator) {
        String[] headerCols = headerLine.split(separator, -1);
        Map<String,Integer> idx = new HashMap<>();
        for (int i = 0; i < headerCols.length; i++) {
            idx.put(headerCols[i].trim(), i);
        }
        List<String> missing = new ArrayList<>();
        for (String key : requiredHeaders) {
            if (!idx.containsKey(key)) missing.add(key);
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Entête invalide : colonnes manquantes -> " + String.join(", ", missing));
        }
        return new Indices(idx);
    }

    /** Accès colonne sécurisé + trim */
    public Function<Integer,String> colAccessor(String[] columns) {
        return i -> {
            if (i == null) return "";
            String v = (i >= 0 && i < columns.length) ? columns[i] : null;
            return v == null ? "" : v.trim();
        };
    }

    /** Valide une ligne et renvoie les erreurs (liste vide si ok) */
    public List<LineErrorDto> validateRow(int lineNumber, Function<Integer,String> col, Indices I) {
        List<LineErrorDto> errs = new ArrayList<>();

        String numeroRNE          = col.apply(I.numeroRNE);
        String raisonSociale      = col.apply(I.raisonSociale);
        String numeroSiret        = col.apply(I.numeroSiret);
        String ActivitePrincipale = col.apply(I.activitePrincipale);
        String codeApe            = col.apply(I.codeApe);
        String codePostal         = col.apply(I.codePostal);
        String mail               = col.apply(I.mail);
        String statutJuridique    = col.apply(I.statutJuridique);

        boolean hasRne   = numeroRNE != null && !numeroRNE.isEmpty();
        boolean hasSiret = numeroSiret != null && !numeroSiret.isEmpty();

        if (!hasRne && !hasSiret) {
            errs.add(new LineErrorDto(lineNumber, "Identifiant", "RNE ou SIRET doit être renseigné", null));
        }
        if (raisonSociale == null || raisonSociale.isEmpty()) {
            errs.add(new LineErrorDto(lineNumber, "Raison sociale", "Champ obligatoire", null));
        }
        if (hasSiret && !numeroSiret.matches("^\\d{14}$")) {
            errs.add(new LineErrorDto(lineNumber, "SIRET", "SIRET invalide (14 chiffres attendus)", numeroSiret));
        }
        if (codeApe != null && !codeApe.isEmpty() && !codeApe.matches("^\\d{2}\\.\\d{2}[A-Z]$")) {
            errs.add(new LineErrorDto(lineNumber, "Code APE", "Format attendu ex: 62.01Z", codeApe));
        }
        if (codePostal != null && !codePostal.isEmpty() && !codePostal.matches("^\\d{5}$")) {
            errs.add(new LineErrorDto(lineNumber, "Code postal", "Code postal FR invalide (5 chiffres)", codePostal));
        }
        if (mail != null && !mail.isEmpty() && !mail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            errs.add(new LineErrorDto(lineNumber, "Email", "Email invalide", mail));
        }
        if ((codeApe == null || codeApe.isEmpty()) && (ActivitePrincipale == null || ActivitePrincipale.isEmpty())){
            errs.add(new LineErrorDto(lineNumber, "Activité", "Code APE ou activité principale attendu",null));
        }
        if(statutJuridique == null || statutJuridique.isEmpty()){
            errs.add(new LineErrorDto(lineNumber, "Statut juridique", "Statut juridique invalide", statutJuridique));
        }
        return errs;
    }

    /** Vérifie doublon et renvoie l’erreur si doublon */
    public Optional<LineErrorDto> duplicateError(
            int lineNumber, Function<Integer,String> col, Indices I, StructureJpaRepository repo) {

        String numeroRNE   = col.apply(I.numeroRNE);
        String numeroSiret = col.apply(I.numeroSiret);

        boolean hasRne   = numeroRNE != null && !numeroRNE.isEmpty();
        boolean hasSiret = numeroSiret != null && !numeroSiret.isEmpty();

        if (!hasRne && hasSiret) {
            if (repo.existAndActifByNumeroSiret(numeroSiret)) {
                return Optional.of(new LineErrorDto(lineNumber, "Doublon", "Déjà présent avec ce SIRET", numeroSiret));
            }
        } else if (hasRne) {
            if (repo.existAndActifByNumeroRNE(numeroRNE)) {
                return Optional.of(new LineErrorDto(lineNumber, "Doublon", "Déjà présent avec ce RNE", numeroRNE));
            }
        }
        return Optional.empty();
    }

    /** Crée une structure à partir des informations récupérées du csv */
    public Structure buildStructure(Function<Integer,String> col, Indices I) {
        String numeroRNE         = col.apply(I.numeroRNE);
        String numeroSiret       = col.apply(I.numeroSiret);
        String typeStructCsv     = (I.typeStructure != null)   ? col.apply(I.typeStructure)   : "";
        String statutCsv         = (I.statutJuridique != null) ? col.apply(I.statutJuridique) : "";
        String effectifCsv       = (I.effectif != null)        ? col.apply(I.effectif)        : "";
        String pays              = (I.pays != null)            ? col.apply(I.pays)            : "";

        Structure s = new Structure();
        s.setPays(resolvePays(pays));
        s.setTypeStructure(resolveTypeStructure(typeStructCsv, numeroRNE, numeroSiret));
        s.setStatutJuridique(resolveStatut(statutCsv));
        s.setEffectif(resolveEffectif(effectifCsv));
        s.setNafN5(resolveApe(col.apply(I.codeApe)));

        s.setNumeroRNE(numeroRNE);
        s.setRaisonSociale(col.apply(I.raisonSociale));
        s.setNumeroSiret(numeroSiret);
        s.setActivitePrincipale(col.apply(I.activitePrincipale));
        s.setVoie(col.apply(I.voie));
        s.setCodePostal(col.apply(I.codePostal));
        s.setCommune(col.apply(I.commune));
        s.setMail(col.apply(I.mail));
        s.setTelephone(col.apply(I.telephone));
        s.setSiteWeb(col.apply(I.siteWeb));
        s.setFax(col.apply(I.fax));
        s.setTemEnServStructure(true);
        return s;
    }

    /* =================== ENCODING UTILS =================== */

    public BufferedReader openReader(InputStream in) throws IOException {
        byte[] bytes = in.readAllBytes();
        Charset charset = detectCharset(bytes);
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset));
    }

    /** Détection BOM/UTF-8 strict, sinon CP1252 */
    private static Charset detectCharset(byte[] bytes) {
        if (startsWith(bytes, new byte[]{(byte)0xEF,(byte)0xBB,(byte)0xBF})) return StandardCharsets.UTF_8;     // UTF-8 BOM
        if (startsWith(bytes, new byte[]{(byte)0xFF,(byte)0xFE}))             return StandardCharsets.UTF_16LE;  // UTF-16 LE BOM
        if (startsWith(bytes, new byte[]{(byte)0xFE,(byte)0xFF}))             return StandardCharsets.UTF_16BE;  // UTF-16 BE BOM

        try {
            CharsetDecoder dec = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            dec.decode(ByteBuffer.wrap(bytes));
            return StandardCharsets.UTF_8;
        } catch (CharacterCodingException ignore) {
            return Charset.forName("windows-1252");
        }
    }

    /* ===== Helpers ===== */

    private Pays resolvePays(String code) {
        Pays p = paysJpaRepository.findByIso2(code);
        if(p != null){
            return p;
        }
        return paysJpaRepository.findByIso2("FR");
    }

    private TypeStructure resolveTypeStructure(String libelle, String numeroRNE, String numeroSiret) {
        TypeStructure t = typeStructureJpaRepository.findByLibelle(libelle);
        if (t == null) {
            if ((numeroRNE != null && !numeroRNE.isEmpty()) && (numeroSiret == null || numeroSiret.isEmpty()) ){
                return typeStructureJpaRepository.findById(7);
            }
            else if ((numeroRNE == null || numeroRNE.isEmpty()) && (numeroSiret != null && !numeroSiret.isEmpty()) ){
                return typeStructureJpaRepository.findById(3);
            } else {
                return typeStructureJpaRepository.findById(6);
            }
        }
        return t;
    }

    private StatutJuridique resolveStatut(String lib) {
        if (lib == null || lib.isBlank()) return null;
        return statutJuridiqueJpaRepository.findByLibelle(lib.trim());
    }

    private Effectif resolveEffectif(String lib) {
        Effectif e = effectifJpaRepository.findByLibelle(lib);
        if (e == null) {
            e = effectifJpaRepository.findById(1);
        }
        return e;
    }

    private NafN5 resolveApe(String code) {
        if (code == null || code.isBlank()) return null;
        return nafN5JpaRepository.findByCode(code.trim());
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data == null || data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }
}