package org.esup_portail.esup_stage.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests des filtres spécifiques de recherche des conventions : chaque clé
 * whitelistée doit produire sa clause JPQL et poser ses paramètres.
 */
class ConventionRepositorySpecificFiltersTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ConventionRepository repository;
    private Query query;

    @BeforeEach
    void setUp() {
        repository = new ConventionRepository(mock(EntityManager.class));
        query = mock(Query.class);
    }

    private JsonNode json(String content) throws Exception {
        return MAPPER.readTree(content);
    }

    private List<String> clausesPour(String key, String parameterJson) throws Exception {
        List<String> clauses = new ArrayList<>();
        repository.addSpecificParameter(key, json(parameterJson), clauses);
        return clauses;
    }

    @Test
    void filtreParPersonnelDuCentre() throws Exception {
        assertThat(clausesPour("centreGestion.personnels", "{\"value\":\"uid1\"}"))
                .containsExactly("personnel.uidPersonnel = :centreGestionpersonnels");

        repository.setSpecificParameterValue("centreGestion.personnels", json("{\"value\":\"uid1\"}"), query);
        verify(query).setParameter("centreGestionpersonnels", "uid1");
    }

    @Test
    void filtreParUidEnseignantEtIdentEtudiant() throws Exception {
        assertThat(clausesPour("enseignant.uidEnseignant", "{\"value\":\"ens1\"}"))
                .containsExactly("c.enseignant.uidEnseignant = :enseignantuidEnseignant");
        assertThat(clausesPour("etudiant.identEtudiant", "{\"value\":\"etu1\"}"))
                .containsExactly("c.etudiant.identEtudiant = :etudiantidentEtudiant");

        repository.setSpecificParameterValue("enseignant.uidEnseignant", json("{\"value\":\"ens1\"}"), query);
        verify(query).setParameter("enseignantuidEnseignant", "ens1");
        repository.setSpecificParameterValue("etudiant.identEtudiant", json("{\"value\":\"etu1\"}"), query);
        verify(query).setParameter("etudiantidentEtudiant", "etu1");
    }

    @Test
    void filtreParEtapesConstruitUnOrParEtape() throws Exception {
        String parametre = "{\"value\":["
                + "{\"code\":\"L3\",\"codeUniversite\":\"UL\",\"codeVersionEtape\":\"1\"},"
                + "{\"code\":\"M1\",\"codeUniversite\":\"UL\",\"codeVersionEtape\":\"2\"}]}";

        List<String> clauses = clausesPour("etape.id", parametre);

        assertThat(clauses).hasSize(1);
        assertThat(clauses.get(0))
                .contains("codeEtape0").contains("codeEtape1")
                .contains(" OR ");

        repository.setSpecificParameterValue("etape.id", json(parametre), query);
        verify(query).setParameter("codeEtape0", "L3");
        verify(query).setParameter("codeUnivEtape0", "UL");
        verify(query).setParameter("versionEtape0", "1");
        verify(query).setParameter("codeEtape1", "M1");
    }

    @Test
    void filtreParEtapesVideNAjouteRien() throws Exception {
        assertThat(clausesPour("etape.id", "{\"value\":[]}")).isEmpty();
    }

    @Test
    void filtreParUfr() throws Exception {
        String parametre = "{\"value\":[{\"code\":\"SCI\",\"codeUniversite\":\"UL\"}]}";

        assertThat(clausesPour("ufr.id", parametre).get(0))
                .contains("c.ufr.id.code = :codeUfr0")
                .contains("c.ufr.id.codeUniversite = :codeUnivUfr0");

        repository.setSpecificParameterValue("ufr.id", json(parametre), query);
        verify(query).setParameter("codeUfr0", "SCI");
        verify(query).setParameter("codeUnivUfr0", "UL");
    }

    @Test
    void rechercheEtudianteCombineChampsEtDecoupageNomPrenom() throws Exception {
        String parametre = "{\"value\":\"Alice Durand\"}";

        List<String> clauses = clausesPour("etudiant", parametre);

        assertThat(clauses).hasSize(1);
        assertThat(clauses.get(0))
                .contains("LOWER(c.etudiant.identEtudiant) LIKE :etudiant")
                .contains(":etudiantSplit0")
                .contains(":etudiantSplit1");

        repository.setSpecificParameterValue("etudiant", json(parametre), query);
        verify(query).setParameter("etudiant", "%alice durand%");
        verify(query).setParameter("etudiantSplit0", "%alice%");
        verify(query).setParameter("etudiantSplit1", "%durand%");
    }

    @Test
    void rechercheEnseignante() throws Exception {
        assertThat(clausesPour("enseignant", "{\"value\":\"Dupont\"}").get(0))
                .contains("LOWER(c.enseignant.nom) LIKE :enseignant");

        repository.setSpecificParameterValue("enseignant", json("{\"value\":\"Dupont\"}"), query);
        verify(query).setParameter("enseignant", "%dupont%");
    }

    @Test
    void filtreAvenantPresentOuAbsent() throws Exception {
        assertThat(clausesPour("avenant", "{\"value\":true}"))
                .containsExactly("(avenant.id IS NOT NULL)");
        assertThat(clausesPour("avenant", "{\"value\":false}"))
                .containsExactly("(avenant.id IS NULL)");
    }

    @Test
    void filtreAccordAnnuaireEnglobeLeNonRenseigneDansLeNon() throws Exception {
        assertThat(clausesPour("accordAnnuaireEtudiant", "{\"value\":true}"))
                .containsExactly("c.accordAnnuaireEtudiant = TRUE");
        // "Non" doit aussi remonter les conventions antérieures, sans réponse enregistrée
        assertThat(clausesPour("accordAnnuaireEtudiant", "{\"value\":false}"))
                .containsExactly("(c.accordAnnuaireEtudiant IS NULL OR c.accordAnnuaireEtudiant = FALSE)");
    }

    @Test
    void etatsDeValidationEtSignature() throws Exception {
        String parametre = "{\"value\":[\"validationPedagogique\",\"validationConvention\",\"verificationAdministrative\","
                + "\"nonValidationPedagogique\",\"nonValidationConvention\",\"nonVerificationAdministrative\","
                + "\"signe\",\"enCours\",\"nonSigne\",\"codeInconnu\"]}";

        List<String> clauses = clausesPour("etatValidation", parametre);

        assertThat(clauses).hasSize(1);
        assertThat(clauses.get(0))
                .contains("c.validationPedagogique = TRUE")
                .contains("c.validationConvention = TRUE")
                .contains("c.verificationAdministrative = TRUE")
                .contains("c.validationPedagogique = FALSE")
                .contains("c.dateSignatureViseur IS NOT NULL")
                .contains("c.dateSignatureViseur IS NULL");
    }

    @Test
    void etatValidationVideNAjouteRien() throws Exception {
        assertThat(clausesPour("etatValidation", "{\"value\":[]}")).isEmpty();
    }

    @Test
    void filtresEtatGestionnaireEtConventionValide() throws Exception {
        assertThat(clausesPour("etatGestionnaire", "{}"))
                .containsExactly("c.validationConvention = FALSE", "c.validationPedagogique = TRUE");
        assertThat(clausesPour("isConventionValide", "{}").get(0))
                .contains("c.centreGestion.validationPedagogique = FALSE OR c.validationPedagogique = TRUE");
    }

    @Test
    void filtreLieuDeStage() throws Exception {
        assertThat(clausesPour("lieuStage", "{\"value\":\"Nancy\"}").get(0))
                .contains("LOWER(c.service.nom) = :lieuStage")
                .contains("LOWER(c.service.pays.lib) = :lieuStage");

        repository.setSpecificParameterValue("lieuStage", json("{\"value\":\"Nancy\"}"), query);
        verify(query).setParameter("lieuStage", "%nancy%");
    }

    @Test
    void filtreStructureEtStageTermine() throws Exception {
        assertThat(clausesPour("structure", "{\"value\":\"ACME\"}").get(0))
                .contains("LOWER(c.structure.raisonSociale) LIKE :structure");
        assertThat(clausesPour("stageTermine", "{\"value\":true}").get(0))
                .contains("c.dateFinStage < CURDATE()");

        repository.setSpecificParameterValue("structure", json("{\"value\":\"ACME\"}"), query);
        verify(query).setParameter("structure", "%acme%");
        repository.setSpecificParameterValue("stageTermine", json("{\"value\":true}"), query);
        verify(query).setParameter("stageTermine", true);
    }

    @Test
    void leTriAjouteNomPrenomEtIdParDefaut() {
        Map<String, String> predicates = repository.orderBy("dateDebutStage", "desc");

        assertThat(predicates)
                .containsEntry("dateDebutStage", "DESC")
                .containsEntry("etudiant.nom", "ASC")
                .containsEntry("etudiant.prenom", "ASC")
                .containsEntry("id", "DESC");
    }

    @Test
    void leTriRespecteLesPredicatsDejaPresents() {
        Map<String, String> predicates = repository.orderBy("etudiant.nom_etudiant.prenom", "desc");

        assertThat(predicates)
                .containsEntry("etudiant.nom", "DESC")
                .containsEntry("etudiant.prenom", "DESC")
                .containsEntry("id", "DESC");
    }
}
