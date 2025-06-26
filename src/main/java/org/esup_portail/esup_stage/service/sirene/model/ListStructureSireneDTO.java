package org.esup_portail.esup_stage.service.sirene.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.esup_portail.esup_stage.model.Structure;

import java.util.List;

@Data
@AllArgsConstructor
public class ListStructureSireneDTO {
    private Integer total;
    private List<Structure> structures;
}
