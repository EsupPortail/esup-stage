package org.esup_portail.esup_stage.events;

import lombok.Data;
import org.esup_portail.esup_stage.model.Structure;

@Data
public class StructureDeletedEvent extends StructureEvent{
    private final Structure structure;
}
