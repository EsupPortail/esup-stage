package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CentreGestionRepositoryTest {

    @Test
    void etablissementExistsReturnsTrueWhenAnEtablissementCentreExists() {
        CentreGestionRepository repository = repositoryWithEtablissementIds(List.of(1));

        assertThat(repository.etablissementExists()).isTrue();
    }

    @Test
    void etablissementExistsReturnsFalseWhenNoEtablissementCentreExists() {
        CentreGestionRepository repository = repositoryWithEtablissementIds(Collections.emptyList());

        assertThat(repository.etablissementExists()).isFalse();
    }

    private CentreGestionRepository repositoryWithEtablissementIds(List<Integer> ids) {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<Integer> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Integer.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(ids);
        return new CentreGestionRepository(entityManager);
    }
}
