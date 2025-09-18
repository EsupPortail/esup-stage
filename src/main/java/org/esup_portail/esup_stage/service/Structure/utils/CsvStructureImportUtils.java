package org.esup_portail.esup_stage.service.Structure.utils;

import org.esup_portail.esup_stage.dto.LineErrorDto;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.StructureJpaRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
public class CsvStructureImportUtils {

    // Noms de colonnes attendus
    private final List<String> requiredHeaders = List.of(
            "NumeroRNE","RaisonSociale","NumeroSiret","ActivitePrincipale",
            "Voie","CodePostal","Commune","Telephone","Fax","SiteWeb","Mail"
    );

    /** Indices mappés depuis l’entête */
    public static final class Indices {
        public final int numeroRNE, raisonSociale, numeroSiret, activitePrincipale,
                voie, codePostal, commune, telephone, fax, siteWeb, mail;

        public Indices(Map<String, Integer> idx) {
            this.numeroRNE          = idx.get("NumeroRNE");
            this.raisonSociale      = idx.get("RaisonSociale");
            this.numeroSiret        = idx.get("NumeroSiret");
            this.activitePrincipale = idx.get("ActivitePrincipale");
            this.voie               = idx.get("Voie");
            this.codePostal         = idx.get("CodePostal");
            this.commune            = idx.get("Commune");
            this.telephone          = idx.get("Telephone");
            this.fax                = idx.get("Fax");
            this.siteWeb            = idx.get("SiteWeb");
            this.mail               = idx.get("Mail");
        }
    }

    /** Mappe les indices à partir de la ligne d’entête, sinon IllegalArgumentException avec la liste manquante */
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
            String v = (i >= 0 && i < columns.length) ? columns[i] : null;
            return v == null ? "" : v.trim();
        };
    }

    /** Valide une ligne et renvoie les erreurs (liste vide si ok) */
    public List<LineErrorDto> validateRow(int lineNumber, Function<Integer,String> col, Indices I) {
        List<LineErrorDto> errs = new ArrayList<>();

        String numeroRNE        = col.apply(I.numeroRNE);
        String raisonSociale    = col.apply(I.raisonSociale);
        String numeroSiret      = col.apply(I.numeroSiret);
        String codePostal       = col.apply(I.codePostal);
        String mail             = col.apply(I.mail);

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
        if (codePostal != null && !codePostal.isEmpty() && !codePostal.matches("^\\d{5}$")) {
            errs.add(new LineErrorDto(lineNumber, "Code postal", "Code postal FR invalide (5 chiffres)", codePostal));
        }
        if (mail != null && !mail.isEmpty() && !mail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            errs.add(new LineErrorDto(lineNumber, "Email", "Email invalide", mail));
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
            if (repo.existByNumeroSiret(numeroSiret)) {
                return Optional.of(new LineErrorDto(lineNumber, "Doublon", "Déjà présent avec ce SIRET", numeroSiret));
            }
        } else if (hasRne) {
            if (repo.existByNumeroRNE(numeroRNE)) {
                return Optional.of(new LineErrorDto(lineNumber, "Doublon", "Déjà présent avec ce RNE", numeroRNE));
            }
        }
        return Optional.empty();
    }

    /** Construit l’entité Structure depuis les colonnes + référentiels */
    public Structure buildStructure(
            Function<Integer,String> col, Indices I,
            NafN5 nafN5, Effectif effectif, StatutJuridique statutJuridique,
            TypeStructure typeStructure, Pays pays) {

        Structure s = new Structure();
        s.setNafN5(nafN5);
        s.setEffectif(effectif);
        s.setStatutJuridique(statutJuridique);
        s.setTypeStructure(typeStructure);
        s.setPays(pays);

        s.setNumeroRNE(col.apply(I.numeroRNE));
        s.setRaisonSociale(col.apply(I.raisonSociale));
        s.setNumeroSiret(col.apply(I.numeroSiret));
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
}