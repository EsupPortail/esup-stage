package org.esup_portail.esup_stage.events;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.esup_portail.esup_stage.model.Structure;

@Data
public class StructureCreatedEvent extends StructureEvent {
    private final Structure structure;
}
