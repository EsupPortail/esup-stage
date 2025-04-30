package org.esup_portail.esup_stage.service.siren.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.esup_portail.esup_stage.model.Structure;

import java.util.List;

@Data
@AllArgsConstructor
public class ListStructureSirenDTO {
    private Integer total;
    private List<Structure> structures;
}
