package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Confidentialite;
import org.esup_portail.esup_stage.model.Contact;
import org.esup_portail.esup_stage.model.Service;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class ConfidentialiteService {

    public static final String PAS_DE_CONFIDENTIALITE = "0";
    public static final String CONFIDENTIALITE_TOTALE = "1";
    public static final String CONFIDENTIALITE_LIBRE = "2";
    public static final String NIVEAU_ETABLISSEMENT = "ETABLISSEMENT";

    @Autowired
    private CentreGestionJpaRepository centreGestionJpaRepository;

    public boolean canViewCentreData(CentreGestion demandeur, CentreGestion porteur) {
        if (demandeur == null || porteur == null) {
            return false;
        }
        if (isSameCentre(demandeur, porteur)) {
            return true;
        }
        return PAS_DE_CONFIDENTIALITE.equals(getEffectiveConfidentialityForCentre(porteur));
    }

    public boolean canViewContact(CentreGestion demandeur, Contact contact) {
        return contact != null && canViewCentreData(demandeur, contact.getCentreGestion());
    }

    public boolean canViewService(CentreGestion demandeur, Service service) {
        return service != null && canViewCentreData(demandeur, service.getCentreGestion());
    }

    public boolean canViewStructureCoordinates(CentreGestion demandeur, Structure structure) {
        if (structure == null) {
            return false;
        }
        if (!structure.isConfidentialiteCoordonnees()) {
            return true;
        }
        if (structure.getCentreGestionProprietaire() == null) {
            return false;
        }
        return canViewCentreData(demandeur, structure.getCentreGestionProprietaire());
    }

    public String getEffectiveConfidentialityForCentre(CentreGestion porteur) {
        if (porteur == null) {
            return PAS_DE_CONFIDENTIALITE;
        }
        if (isCentreEtablissement(porteur)) {
            return resolveEtablissementEffectiveConfidentiality(porteur);
        }
        CentreGestion etablissement = centreGestionJpaRepository.getCentreEtablissement();
        String etablissementCode = getConfiguredConfidentialityCode(etablissement);
        if (PAS_DE_CONFIDENTIALITE.equals(etablissementCode) || CONFIDENTIALITE_TOTALE.equals(etablissementCode)) {
            return etablissementCode;
        }
        String centreCode = getConfiguredConfidentialityCode(porteur);
        return CONFIDENTIALITE_LIBRE.equals(centreCode) ? CONFIDENTIALITE_TOTALE : centreCode;
    }

    private String resolveEtablissementEffectiveConfidentiality(CentreGestion etablissement) {
        String configuredCode = getConfiguredConfidentialityCode(etablissement);
        if (!CONFIDENTIALITE_LIBRE.equals(configuredCode)) {
            return configuredCode;
        }
        Confidentialite effectiveOrpheline = etablissement.getCodeConfidentialiteConventionOrpheline();
        if (effectiveOrpheline != null
                && effectiveOrpheline.getCode() != null
                && !effectiveOrpheline.getCode().isBlank()
                && !CONFIDENTIALITE_LIBRE.equals(effectiveOrpheline.getCode())) {
            return effectiveOrpheline.getCode();
        }
        return CONFIDENTIALITE_TOTALE;
    }

    public boolean isNoConfidentiality(CentreGestion centreGestion) {
        return PAS_DE_CONFIDENTIALITE.equals(getEffectiveConfidentialityForCentre(centreGestion));
    }

    public boolean isCentreEtablissement(CentreGestion centreGestion) {
        return centreGestion != null
                && centreGestion.getNiveauCentre() != null
                && NIVEAU_ETABLISSEMENT.equalsIgnoreCase(centreGestion.getNiveauCentre().getLibelle());
    }

    private String getConfiguredConfidentialityCode(CentreGestion centreGestion) {
        if (centreGestion == null) {
            return PAS_DE_CONFIDENTIALITE;
        }
        Confidentialite confidentialite = centreGestion.getCodeConfidentialite();
        if (confidentialite == null || confidentialite.getCode() == null || confidentialite.getCode().isBlank()) {
            return PAS_DE_CONFIDENTIALITE;
        }
        return confidentialite.getCode();
    }

    private boolean isSameCentre(CentreGestion demandeur, CentreGestion porteur) {
        return demandeur != null
                && porteur != null
                && demandeur.getId() == porteur.getId();
    }
}