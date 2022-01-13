package org.esup_portail.esup_stage.dto;

import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.TypeConvention;
import org.esup_portail.esup_stage.service.apogee.model.ElementPedagogique;
import org.esup_portail.esup_stage.service.apogee.model.EtapeInscription;

import java.util.ArrayList;
import java.util.List;

public class ConventionFormationDto {
    private EtapeInscription etapeInscription;
    private String annee;
    private List<ElementPedagogique> elementPedagogiques = new ArrayList<>();
    private CentreGestion centreGestion;
    private TypeConvention typeConvention;


    public EtapeInscription getEtapeInscription() {
        return etapeInscription;
    }

    public void setEtapeInscription(EtapeInscription etapeInscription) {
        this.etapeInscription = etapeInscription;
    }

    public String getAnnee() {
        return annee;
    }

    public void setAnnee(String annee) {
        this.annee = annee;
    }

    public List<ElementPedagogique> getElementPedagogiques() {
        return elementPedagogiques;
    }

    public void setElementPedagogiques(List<ElementPedagogique> elementPedagogiques) {
        this.elementPedagogiques = elementPedagogiques;
    }

    public CentreGestion getCentreGestion() {
        return centreGestion;
    }

    public void setCentreGestion(CentreGestion centreGestion) {
        this.centreGestion = centreGestion;
    }

    public TypeConvention getTypeConvention() {
        return typeConvention;
    }

    public void setTypeConvention(TypeConvention typeConvention) {
        this.typeConvention = typeConvention;
    }
}
