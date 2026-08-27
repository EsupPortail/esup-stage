package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaginationRepositoryTest {

    private EntityManager em;
    private Metamodel metamodel;
    private ManagedType<Utilisateur> managedType;
    private TypedQuery<Long> countQuery;
    private UtilisateurRepository repository;

    @BeforeEach
    void setup() {
        em = mock(EntityManager.class);
        metamodel = mock(Metamodel.class);
        managedType = mock(ManagedType.class);
        countQuery = mock(TypedQuery.class);
        repository = new UtilisateurRepository(em);

        when(em.getMetamodel()).thenReturn(metamodel);
        when(metamodel.managedType(Utilisateur.class)).thenReturn(managedType);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getParameters()).thenReturn(Collections.emptySet());
        when(countQuery.getSingleResult()).thenReturn(1L);
    }

    @Test
    void countUsesGeneratedParameterNameForValidStandardFilter() {
        Attribute loginAttribute = mock(Attribute.class);
        when(managedType.getAttribute("login")).thenReturn(loginAttribute);

        repository.count("{\"login\":{\"type\":\"text\",\"value\":\"admin\"}}");

        verify(em).createQuery(contains("LOWER(u.login) LIKE :filter0"), eq(Long.class));
        verify(countQuery).setParameter("filter0", "%admin%");
    }

    @Test
    void countRejectsMalformedFilterKeyBeforeCreatingQuery() {
        assertThatThrownBy(() -> repository.count("{\"login) OR 1=1 OR (u.login\":{\"type\":\"text\",\"value\":\"admin\"}}"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Filtre invalide");

        verify(em, never()).createQuery(anyString(), eq(Long.class));
    }

    @Test
    void countRejectsUnknownStandardFilterPath() {
        when(managedType.getAttribute("unknown")).thenThrow(new IllegalArgumentException("unknown attribute"));

        assertThatThrownBy(() -> repository.count("{\"unknown\":{\"type\":\"text\",\"value\":\"admin\"}}"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Filtre non autorisé");

        verify(em, never()).createQuery(anyString(), eq(Long.class));
    }

    @Test
    void countRejectsUnknownSpecificFilter() {
        assertThatThrownBy(() -> repository.count("{\"login\":{\"type\":\"text\",\"value\":\"admin\",\"specific\":true}}"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Filtre spécifique non autorisé");

        verify(em, never()).createQuery(anyString(), eq(Long.class));
    }

    @Test
    void countTreatsBlankFiltersAsEmptyFilters() {
        repository.count("");

        verify(em).createQuery(contains("SELECT COUNT(DISTINCT u) FROM org.esup_portail.esup_stage.model.Utilisateur u "), eq(Long.class));
        verify(em, never()).createQuery(contains(" WHERE "), eq(Long.class));
    }

    @Test
    void countDoesNotAppendWhereForSpecificFilterWithoutClause() {
        TypeConventionRepository typeConventionRepository = new TypeConventionRepository(em);
        ManagedType typeConventionManagedType = mock(ManagedType.class);
        when(metamodel.managedType(org.esup_portail.esup_stage.model.TypeConvention.class)).thenReturn(typeConventionManagedType);

        typeConventionRepository.count("{\"templatePDF\":{\"type\":\"boolean\",\"value\":true,\"specific\":true}}");

        verify(em).createQuery(contains("JOIN tc.templates template"), eq(Long.class));
        verify(em, never()).createQuery(contains(" WHERE "), eq(Long.class));
    }
}
