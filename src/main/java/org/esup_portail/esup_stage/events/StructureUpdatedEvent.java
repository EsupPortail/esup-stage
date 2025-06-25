package org.esup_portail.esup_stage.events;

import lombok.Data;
import org.esup_portail.esup_stage.model.Structure;

@Data
public class StructureUpdatedEvent extends StructureEvent {
    private final Structure structure;
    private final String oldStructure;
    private final String newStructure;
}
