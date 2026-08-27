package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.esup_portail.esup_stage.model.LangueConvention;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LangueConventionRepositoryTest {

    @Test
    void existsReturnsFalseWhenNoLangueConventionHasThisCode() {
        LangueConventionRepository repository = repositoryWithCodesInDatabase(Collections.emptyList());

        LangueConvention nouvelleLangue = new LangueConvention();
        nouvelleLangue.setCode("XX");

        assertThat(repository.exists(nouvelleLangue)).isFalse();
    }

    @Test
    void existsReturnsTrueWhenALangueConventionAlreadyHasThisCode() {
        LangueConventionRepository repository = repositoryWithCodesInDatabase(List.of("FR"));

        LangueConvention langueExistante = new LangueConvention();
        langueExistante.setCode("FR");

        assertThat(repository.exists(langueExistante)).isTrue();
    }

    private LangueConventionRepository repositoryWithCodesInDatabase(List<String> existingCodes) {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<String> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(existingCodes);
        return new LangueConventionRepository(entityManager);
    }
}
