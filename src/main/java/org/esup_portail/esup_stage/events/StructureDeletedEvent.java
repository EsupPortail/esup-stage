package org.esup_portail.esup_stage.events;

import org.esup_portail.esup_stage.model.Structure;

public class StructureDeletedEvent extends StructureEvent{
    private final Structure structure;

    public StructureDeletedEvent(Structure structure) {
        this.structure = structure;
    }

    public Structure getStructure() {
        return structure;
    }
}
