package org.esup_portail.esup_stage.repository;

import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.model.TypeConvention;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests de régression du commit « protection supplémentaire injection JPQL »
 * qui avait inversé des conditions size() > 0 en isEmpty() :
 * - exists() renvoyait « déjà existant » pour tout libellé/code inédit
 *   (création de nomenclature impossible) ;
 * - le filtre par étapes des étudiants de groupe était ignoré.
 */
class RepositoryExistsRegressionTest {

    private EntityManager entityManagerRenvoyant(List<Integer> ids) {
        EntityManager entityManager = mock(EntityManager.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Integer> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Integer.class))).thenReturn(query);
        when(query.setParameter(anyString(), org.mockito.ArgumentMatchers.any())).thenReturn(query);
        when(query.getResultList()).thenReturn(ids);
        return entityManager;
    }

    // ------------------------------------------------------------------
    // PaginationRepository.exists (libellé) — utilisé par les nomenclatures
    // ------------------------------------------------------------------

    private PaginationRepository<TypeConvention> paginationRepository(List<Integer> ids) {
        return new PaginationRepository<>(entityManagerRenvoyant(ids), TypeConvention.class, "tc");
    }

    @Test
    void creationAvecLibelleIneditEstPossible() {
        assertThat(paginationRepository(Collections.emptyList()).exists("Nouveau libellé", 0)).isFalse();
    }

    @Test
    void creationAvecLibelleDejaPrisEstRefusee() {
        assertThat(paginationRepository(List.of(3)).exists("Doublon", 0)).isTrue();
    }

    @Test
    void modificationSansChangerSonPropreLibelleEstPossible() {
        assertThat(paginationRepository(List.of(3)).exists("Mon libellé", 3)).isFalse();
    }

    @Test
    void modificationVersLeLibelleDUnAutreEstRefusee() {
        assertThat(paginationRepository(List.of(4)).exists("Libellé de l'autre", 3)).isTrue();
    }

    // ------------------------------------------------------------------
    // TypeConventionRepository.exists (codeCtrl)
    // ------------------------------------------------------------------

    @Test
    void typeConventionCodeIneditEstPossible() {
        TypeConventionRepository repository = new TypeConventionRepository(entityManagerRenvoyant(Collections.emptyList()));
        assertThat(repository.exists("CODE_NEUF", 0)).isFalse();
    }

    @Test
    void typeConventionCodeDejaPrisEstRefuse() {
        TypeConventionRepository repository = new TypeConventionRepository(entityManagerRenvoyant(List.of(9)));
        assertThat(repository.exists("CODE_PRIS", 0)).isTrue();
    }

    // ------------------------------------------------------------------
    // StructureRepository.existsSiret
    // ------------------------------------------------------------------

    @Test
    void structureAvecSiretIneditEstPossible() {
        StructureRepository repository = new StructureRepository(entityManagerRenvoyant(Collections.emptyList()));
        Structure structure = new Structure();
        structure.setId(0);
        assertThat(repository.existsSiret(structure, "12345678901234")).isFalse();
    }

    @Test
    void structureAvecSiretDejaPrisEstRefusee() {
        StructureRepository repository = new StructureRepository(entityManagerRenvoyant(List.of(8)));
        Structure structure = new Structure();
        structure.setId(0);
        assertThat(repository.existsSiret(structure, "12345678901234")).isTrue();
    }

    @Test
    void structureConserveSonPropreSiret() {
        StructureRepository repository = new StructureRepository(entityManagerRenvoyant(List.of(8)));
        Structure structure = new Structure();
        structure.setId(8);
        assertThat(repository.existsSiret(structure, "12345678901234")).isFalse();
    }

    // ------------------------------------------------------------------
    // EtudiantGroupeEtudiantRepository : filtre par étapes
    // ------------------------------------------------------------------

    @Test
    void filtreParEtapesAjouteLaClauseOr() throws Exception {
        EtudiantGroupeEtudiantRepository repository =
                new EtudiantGroupeEtudiantRepository(mock(EntityManager.class));
        List<String> clauses = new ArrayList<>();
        var parameter = new ObjectMapper().readTree(
                "{\"value\":[{\"code\":\"L3\",\"codeUniversite\":\"UL\",\"codeVersionEtape\":\"1\"}]}");

        repository.addSpecificParameter("etape.id", parameter, clauses);

        assertThat(clauses).hasSize(1);
        assertThat(clauses.get(0)).contains("codeEtape0").contains("codeUnivEtape0").contains("versionEtape0");
    }

    @Test
    void filtreParEtapesVideNAjouteAucuneClause() throws Exception {
        EtudiantGroupeEtudiantRepository repository =
                new EtudiantGroupeEtudiantRepository(mock(EntityManager.class));
        List<String> clauses = new ArrayList<>();
        var parameter = new ObjectMapper().readTree("{\"value\":[]}");

        repository.addSpecificParameter("etape.id", parameter, clauses);

        assertThat(clauses).as("pas de clause '()' malformée").isEmpty();
    }
}
