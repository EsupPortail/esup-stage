package org.esup_portail.esup_stage.events;

import org.esup_portail.esup_stage.model.Structure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StructureEventsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Structure structure() {
        Structure structure = new Structure();
        structure.setId(42);
        structure.setRaisonSociale("ACME");
        return structure;
    }

    @Test
    void structureCreatedEventPorteLaStructureEtUnHorodatage() {
        LocalDateTime avant = LocalDateTime.now();
        StructureCreatedEvent event = new StructureCreatedEvent(structure());

        assertThat(event.getStructure().getRaisonSociale()).isEqualTo("ACME");
        assertThat(event.getTimestamp()).isAfterOrEqualTo(avant).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(event.getUtilisateur()).as("aucun utilisateur connecté en test").isNull();
        assertThat(event.toString()).isNotNull();
    }

    @Test
    void structureUpdatedEventPorteLaStructureEtLesEtats() {
        StructureUpdatedEvent event = new StructureUpdatedEvent(structure(), "avant", "après", true);

        assertThat(event.getStructure().getId()).isEqualTo(42);
        assertThat(event.getOldStructure()).isEqualTo("avant");
        assertThat(event.getNewStructure()).isEqualTo("après");
        assertThat(event.isAuto()).isTrue();
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    void structureDeletedEventPorteLaStructure() {
        StructureDeletedEvent event = new StructureDeletedEvent(structure());

        assertThat(event.getStructure().getId()).isEqualTo(42);
        assertThat(event.getTimestamp()).isNotNull();
    }
}
