package org.esup_portail.esup_stage.service.Structure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.events.StructureCreatedEvent;
import org.esup_portail.esup_stage.events.StructureDeletedEvent;
import org.esup_portail.esup_stage.events.StructureUpdatedEvent;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.repository.StructureJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service métier de gestion des structures. Aucune infrastructure
 * Spring : le service est instancié à la main et ses dépendances (repository, publisher
 * d'événements, ObjectMapper) sont mockées et injectées par réflexion.
 */
class StructureServiceTest {

    private StructureService service;
    private StructureJpaRepository structureJpaRepository;
    private ApplicationEventPublisher eventPublisher;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        service = new StructureService();
        structureJpaRepository = mock(StructureJpaRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        objectMapper = mock(ObjectMapper.class);
        ReflectionTestUtils.setField(service, "structureJpaRepository", structureJpaRepository);
        ReflectionTestUtils.setField(service, "eventPublisher", eventPublisher);
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
    }

    private Structure structure() {
        Structure structure = new Structure();
        structure.setId(7);
        structure.setRaisonSociale("ACME");
        return structure;
    }

    @Test
    void saveNouvelleStructurePublieUnCreatedEvent() {
        Structure structure = structure();
        when(structureJpaRepository.save(structure)).thenReturn(structure);

        Structure result = service.save(null, structure);

        assertThat(result).isSameAs(structure);
        verify(eventPublisher).publishEvent(any(StructureCreatedEvent.class));
        verify(eventPublisher, never()).publishEvent(any(StructureUpdatedEvent.class));
    }

    @Test
    void saveStructureExistantePublieUnUpdatedEvent() throws Exception {
        Structure structure = structure();
        when(structureJpaRepository.save(structure)).thenReturn(structure);
        when(objectMapper.writeValueAsString(structure)).thenReturn("{\"raisonSociale\":\"ACME\"}");

        Structure result = service.save("{\"raisonSociale\":\"OLD\"}", structure);

        assertThat(result).isSameAs(structure);
        verify(eventPublisher).publishEvent(any(StructureUpdatedEvent.class));
        verify(eventPublisher, never()).publishEvent(any(StructureCreatedEvent.class));
    }

    @Test
    void saveStructureExistanteEncapsuleLErreurDeSerialisation() throws Exception {
        Structure structure = structure();
        when(structureJpaRepository.save(structure)).thenReturn(structure);
        when(objectMapper.writeValueAsString(structure))
                .thenThrow(new JsonProcessingException("boom") {});

        assertThatThrownBy(() -> service.save("{\"raisonSociale\":\"OLD\"}", structure))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("sérialisation")
                .hasCauseInstanceOf(JsonProcessingException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deleteDesactiveLaStructureEtPublieUnDeletedEvent() {
        Structure structure = structure();
        structure.setTemEnServStructure(true);

        service.delete(structure);

        assertThat(structure.getTemEnServStructure()).isFalse();
        verify(structureJpaRepository).saveAndFlush(structure);
        verify(eventPublisher).publishEvent(any(StructureDeletedEvent.class));
    }
}
