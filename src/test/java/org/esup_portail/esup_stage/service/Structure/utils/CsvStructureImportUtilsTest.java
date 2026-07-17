package org.esup_portail.esup_stage.service.Structure.utils;

import org.esup_portail.esup_stage.dto.LineErrorDto;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsvStructureImportUtilsTest {

    private static final String ENTETE = String.join(";",
            "NumeroRNE", "RaisonSociale", "NumeroSiret", "ActivitePrincipale",
            "CodeAPE", "Voie", "CodePostal", "Commune", "Telephone", "Fax", "SiteWeb", "Mail",
            "TypeStructure", "StatutJuridique", "Effectif", "Pays");

    private CsvStructureImportUtils utils;
    private NafN5JpaRepository nafN5JpaRepository;
    private EffectifJpaRepository effectifJpaRepository;
    private StatutJuridiqueJpaRepository statutJuridiqueJpaRepository;
    private TypeStructureJpaRepository typeStructureJpaRepository;
    private PaysJpaRepository paysJpaRepository;

    @BeforeEach
    void setUp() {
        utils = new CsvStructureImportUtils();
        nafN5JpaRepository = mock(NafN5JpaRepository.class);
        effectifJpaRepository = mock(EffectifJpaRepository.class);
        statutJuridiqueJpaRepository = mock(StatutJuridiqueJpaRepository.class);
        typeStructureJpaRepository = mock(TypeStructureJpaRepository.class);
        paysJpaRepository = mock(PaysJpaRepository.class);
        ReflectionTestUtils.setField(utils, "nafN5JpaRepository", nafN5JpaRepository);
        ReflectionTestUtils.setField(utils, "effectifJpaRepository", effectifJpaRepository);
        ReflectionTestUtils.setField(utils, "statutJuridiqueJpaRepository", statutJuridiqueJpaRepository);
        ReflectionTestUtils.setField(utils, "typeStructureJpaRepository", typeStructureJpaRepository);
        ReflectionTestUtils.setField(utils, "paysJpaRepository", paysJpaRepository);
    }

    private CsvStructureImportUtils.Indices indices() {
        return utils.mapHeaderIndices(ENTETE, ";");
    }

    private Function<Integer, String> ligne(Map<String, String> valeurs) {
        String[] colonnes = ENTETE.split(";", -1);
        String[] cells = new String[colonnes.length];
        for (int i = 0; i < colonnes.length; i++) {
            cells[i] = valeurs.getOrDefault(colonnes[i], "");
        }
        return utils.colAccessor(cells);
    }

    @Test
    void mapHeaderIndicesResoutToutesLesColonnes() {
        CsvStructureImportUtils.Indices indices = indices();

        assertThat(indices.numeroRNE).isZero();
        assertThat(indices.raisonSociale).isEqualTo(1);
        assertThat(indices.pays).isEqualTo(15);
    }

    @Test
    void mapHeaderIndicesSignaleLesColonnesManquantes() {
        assertThatThrownBy(() -> utils.mapHeaderIndices("NumeroRNE;RaisonSociale", ";"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NumeroSiret")
                .hasMessageContaining("Pays");
    }

    @Test
    void colAccessorEstTolerantAuxIndicesInvalides() {
        Function<Integer, String> col = utils.colAccessor(new String[]{" a ", null});

        assertThat(col.apply(0)).isEqualTo("a");
        assertThat(col.apply(1)).isEmpty();
        assertThat(col.apply(5)).isEmpty();
        assertThat(col.apply(-1)).isEmpty();
        assertThat(col.apply(null)).isEmpty();
    }

    @Test
    void ligneValideNeProduitAucuneErreur() {
        List<LineErrorDto> erreurs = utils.validateRow(2, ligne(Map.of(
                "NumeroSiret", "12345678901234",
                "RaisonSociale", "ACME",
                "CodeAPE", "62.01Z",
                "CodePostal", "54000",
                "Mail", "contact@acme.fr",
                "StatutJuridique", "SAS"
        )), indices());

        assertThat(erreurs).isEmpty();
    }

    @Test
    void chaqueRegleDeValidationEstControlee() {
        List<LineErrorDto> erreurs = utils.validateRow(3, ligne(Map.of(
                "NumeroSiret", "123",           // SIRET invalide
                "CodeAPE", "invalid",           // APE invalide
                "CodePostal", "5400",           // CP invalide
                "Mail", "pas-un-mail"           // mail invalide
                // RaisonSociale absente, StatutJuridique absent
        )), indices());

        assertThat(erreurs).extracting(LineErrorDto::getField)
                .containsExactlyInAnyOrder("Raison sociale", "SIRET", "Code APE", "Code postal", "Email", "Statut juridique");
        assertThat(erreurs).allSatisfy(e -> assertThat(e.getLine()).isEqualTo(3));
    }

    @Test
    void identifiantRneOuSiretObligatoire() {
        List<LineErrorDto> erreurs = utils.validateRow(4, ligne(Map.of(
                "RaisonSociale", "ACME",
                "ActivitePrincipale", "Conseil",
                "StatutJuridique", "SAS"
        )), indices());

        assertThat(erreurs).extracting(LineErrorDto::getField).containsExactly("Identifiant");
    }

    @Test
    void activiteOuCodeApeObligatoire() {
        List<LineErrorDto> erreurs = utils.validateRow(5, ligne(Map.of(
                "NumeroRNE", "0540099X",
                "RaisonSociale", "Lycée",
                "StatutJuridique", "Public"
        )), indices());

        assertThat(erreurs).extracting(LineErrorDto::getField).containsExactly("Activité");
    }

    @Test
    void doublonSiretEstDetecte() {
        StructureJpaRepository repo = mock(StructureJpaRepository.class);
        when(repo.existAndActifByNumeroSiret("12345678901234")).thenReturn(true);

        Optional<LineErrorDto> erreur = utils.duplicateError(6, ligne(Map.of(
                "NumeroSiret", "12345678901234"
        )), indices(), repo);

        assertThat(erreur).isPresent();
        assertThat(erreur.get().getMessage()).contains("SIRET");
    }

    @Test
    void doublonRneEstDetecte() {
        StructureJpaRepository repo = mock(StructureJpaRepository.class);
        when(repo.existAndActifByNumeroRNE("0540099X")).thenReturn(true);

        Optional<LineErrorDto> erreur = utils.duplicateError(7, ligne(Map.of(
                "NumeroRNE", "0540099X",
                "NumeroSiret", "12345678901234"
        )), indices(), repo);

        assertThat(erreur).isPresent();
        assertThat(erreur.get().getMessage()).contains("RNE");
    }

    @Test
    void absenceDeDoublonRenvoieVide() {
        StructureJpaRepository repo = mock(StructureJpaRepository.class);
        when(repo.existAndActifByNumeroSiret("12345678901234")).thenReturn(false);

        Optional<LineErrorDto> erreur = utils.duplicateError(8, ligne(Map.of(
                "NumeroSiret", "12345678901234"
        )), indices(), repo);

        assertThat(erreur).isEmpty();
    }

    @Test
    void buildStructureResoutLesReferentiels() {
        Pays france = new Pays();
        when(paysJpaRepository.findByIso2("FR")).thenReturn(france);
        TypeStructure typeStructure = new TypeStructure();
        when(typeStructureJpaRepository.findByLibelle("Entreprise")).thenReturn(typeStructure);
        StatutJuridique statutJuridique = new StatutJuridique();
        when(statutJuridiqueJpaRepository.findByLibelle("SAS")).thenReturn(statutJuridique);
        Effectif effectif = new Effectif();
        when(effectifJpaRepository.findByLibelle("50-100")).thenReturn(effectif);
        NafN5 naf = new NafN5();
        when(nafN5JpaRepository.findByCode("62.01Z")).thenReturn(naf);

        Structure structure = utils.buildStructure(ligne(Map.of(
                "NumeroSiret", "12345678901234",
                "RaisonSociale", "ACME",
                "CodeAPE", "62.01Z",
                "Voie", "1 rue des Lilas",
                "CodePostal", "54000",
                "Commune", "Nancy",
                "Mail", "contact@acme.fr",
                "TypeStructure", "Entreprise",
                "StatutJuridique", "SAS",
                "Effectif", "50-100"
        )), indices());

        assertThat(structure.getNumeroSiret()).isEqualTo("12345678901234");
        assertThat(structure.getRaisonSociale()).isEqualTo("ACME");
        assertThat(structure.getPays()).isSameAs(france);
        assertThat(structure.getTypeStructure()).isSameAs(typeStructure);
        assertThat(structure.getStatutJuridique()).isSameAs(statutJuridique);
        assertThat(structure.getEffectif()).isSameAs(effectif);
        assertThat(structure.getNafN5()).isSameAs(naf);
        assertThat(structure.getTemEnServStructure()).isTrue();
    }

    @Test
    void buildStructureAvecRneSeulDonneUnEtablissementEnseignement() {
        when(paysJpaRepository.findByIso2("FR")).thenReturn(new Pays());
        when(typeStructureJpaRepository.findByLibelle("")).thenReturn(null);
        TypeStructure enseignement = new TypeStructure();
        when(typeStructureJpaRepository.findById(7)).thenReturn(enseignement);
        Effectif effectifParDefaut = new Effectif();
        when(effectifJpaRepository.findById(1)).thenReturn(effectifParDefaut);

        Structure structure = utils.buildStructure(ligne(Map.of(
                "NumeroRNE", "0540099X",
                "RaisonSociale", "Lycée",
                "ActivitePrincipale", "Enseignement"
        )), indices());

        assertThat(structure.getTypeStructure()).isSameAs(enseignement);
        assertThat(structure.getEffectif()).isSameAs(effectifParDefaut);
        assertThat(structure.getStatutJuridique()).isNull();
        assertThat(structure.getNafN5()).isNull();
    }

    @Test
    void buildStructureAvecSiretSeulDonneUneEntreprisePrivee() {
        when(paysJpaRepository.findByIso2("FR")).thenReturn(new Pays());
        TypeStructure entreprise = new TypeStructure();
        when(typeStructureJpaRepository.findById(3)).thenReturn(entreprise);

        Structure structure = utils.buildStructure(ligne(Map.of(
                "NumeroSiret", "12345678901234",
                "RaisonSociale", "ACME"
        )), indices());

        assertThat(structure.getTypeStructure()).isSameAs(entreprise);
    }

    @Test
    void openReaderDetecteLeBomUtf8() throws IOException {
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] contenu = "col1;col2".getBytes(StandardCharsets.UTF_8);
        byte[] fichier = new byte[bom.length + contenu.length];
        System.arraycopy(bom, 0, fichier, 0, bom.length);
        System.arraycopy(contenu, 0, fichier, bom.length, contenu.length);

        BufferedReader reader = utils.openReader(new ByteArrayInputStream(fichier));

        assertThat(reader.readLine()).endsWith("col1;col2");
    }

    @Test
    void openReaderLitLUtf8SansBom() throws IOException {
        BufferedReader reader = utils.openReader(
                new ByteArrayInputStream("Société générale".getBytes(StandardCharsets.UTF_8)));

        assertThat(reader.readLine()).isEqualTo("Société générale");
    }

    @Test
    void openReaderBasculeEnWindows1252PourLesAccentsLatins() throws IOException {
        // "é" encodé en CP1252 (0xE9) n'est pas de l'UTF-8 valide
        byte[] cp1252 = "Société".getBytes(java.nio.charset.Charset.forName("windows-1252"));

        BufferedReader reader = utils.openReader(new ByteArrayInputStream(cp1252));

        assertThat(reader.readLine()).isEqualTo("Société");
    }
}
