package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.TypeConvention;
import org.esup_portail.esup_stage.service.apogee.model.ElementPedagogique;
import org.esup_portail.esup_stage.service.apogee.model.EtapeInscription;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConventionFormationDto {
    private EtapeInscription etapeInscription;
    private String annee;
    private List<ElementPedagogique> elementPedagogiques = new ArrayList<>();
    private CentreGestion centreGestion;

    // Legacy field kept for compatibility (preselected candidate when unique)
    private TypeConvention typeConvention;

    private List<TypeConvention> typeConventions = new ArrayList<>();
    private TypeConventionSelectionMode typeConventionSelectionMode = TypeConventionSelectionMode.AUTO_NO_CANDIDATE;
    private String typeConventionSelectionMessage;

}