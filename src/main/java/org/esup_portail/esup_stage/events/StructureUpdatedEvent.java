package org.esup_portail.esup_stage.events;

import org.esup_portail.esup_stage.model.Structure;

public class StructureUpdatedEvent extends StructureEvent {
    private final Structure oldStructure;
    private final Structure newStructure;

    public StructureUpdatedEvent(Structure oldStructure, Structure newStructure) {
        this.oldStructure = oldStructure;
        this.newStructure = newStructure;
    }

    public Structure getOldStructure() {
        return oldStructure;
    }

    public Structure getNewStructure() {
        return newStructure;
    }
}
