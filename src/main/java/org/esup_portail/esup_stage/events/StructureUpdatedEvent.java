package org.esup_portail.esup_stage.events;

import lombok.Data;
import org.esup_portail.esup_stage.model.Structure;

@Data
public class StructureUpdatedEvent extends StructureEvent {
    private final Structure oldStructure;
    private final Structure newStructure;
}
