package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Confidentialite;
import org.esup_portail.esup_stage.model.Contact;
import org.esup_portail.esup_stage.model.Service;
import org.esup_portail.esup_stage.model.Structure;

@org.springframework.stereotype.Service
public class ConfidentialiteService {

    public static final String PAS_DE_CONFIDENTIALITE = "0";
    public static final String CONFIDENTIALITE_TOTALE = "1";
    public static final String CONFIDENTIALITE_LIBRE = "2";
    public static final String NIVEAU_ETABLISSEMENT = "ETABLISSEMENT";

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

    public boolean canViewContact(CentreGestion demandeur, CentreGestion porteur) {
        return canViewCentreData(demandeur, porteur);
    }

    public boolean canViewService(CentreGestion demandeur, Service service) {
        return service != null && canViewCentreData(demandeur, service.getCentreGestion());
    }

    public boolean canViewService(CentreGestion demandeur, CentreGestion porteur) {
        return canViewCentreData(demandeur, porteur);
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

    public boolean canViewStructureCoordinates(CentreGestion demandeur, Structure structure, CentreGestion porteur) {
        if (structure == null) {
            return false;
        }
        if (!structure.isConfidentialiteCoordonnees()) {
            return true;
        }
        return porteur != null && canViewCentreData(demandeur, porteur);
    }

    public boolean canViewStructureCoordinates(CentreGestion demandeur, CentreGestion porteur) {
        return canViewCentreData(demandeur, porteur);
    }

    public String getEffectiveConfidentialityForCentre(CentreGestion porteur) {
        String configuredCode = getConfiguredConfidentialityCode(porteur);
        if (!CONFIDENTIALITE_LIBRE.equals(configuredCode)) {
            return configuredCode;
        }

        Confidentialite effectiveOrpheline = porteur != null ? porteur.getCodeConfidentialiteConventionOrpheline() : null;
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

    public boolean isTotalConfidentiality(CentreGestion centreGestion) {
        return CONFIDENTIALITE_TOTALE.equals(getEffectiveConfidentialityForCentre(centreGestion));
    }

    public boolean isFreeConfidentiality(CentreGestion centreGestion) {
        return CONFIDENTIALITE_LIBRE.equals(getConfiguredConfidentialityCode(centreGestion));
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