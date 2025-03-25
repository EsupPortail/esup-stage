package org.esup_portail.esup_stage.events;

import org.esup_portail.esup_stage.model.Structure;

public class StructureCreatedEvent extends StructureEvent {
    private final Structure structure;

    public StructureCreatedEvent(Structure structure) {
        this.structure = structure;
    }

    public Structure getStructure() {
        return structure;
    }
}
