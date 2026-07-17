package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Contact;
import org.esup_portail.esup_stage.model.Service;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Source unique des décisions de visibilité liées à la confidentialité des centres de gestion
 * (contacts, services, coordonnées d'organisme). Centralise la logique auparavant dupliquée dans
 * les contrôleurs Contact/Service/Structure afin d'éviter qu'un appelant l'oublie ou la diverge.
 */
@org.springframework.stereotype.Service
public class ConfidentialiteAccessService {

    @Autowired
    private HabilitationService habilitationService;

    @Autowired
    private ConfidentialiteService confidentialiteService;

    @Autowired
    private CentreGestionJpaRepository centreGestionJpaRepository;

    public boolean isGestionnaire(Utilisateur utilisateur) {
        return habilitationService.isGestionnaire(utilisateur);
    }

    /**
     * Centres de gestion sur lesquels l'utilisateur possède un rôle gestionnaire (rôle appliqué
     * sur le centre) : périmètre à partir duquel la confidentialité est évaluée.
     */
    public List<CentreGestion> getCentresDemandeur(Utilisateur utilisateur) {
        List<Integer> centreIds = habilitationService.getGestionnaireCentreIds(utilisateur);
        if (centreIds.isEmpty()) {
            return new ArrayList<>();
        }
        return centreGestionJpaRepository.findAllById(centreIds);
    }

    /**
     * Identifiants des centres dont les données sont visibles depuis {@code centresDemandeur} :
     * les centres de rattachement eux-mêmes, plus ceux dont la confidentialité effective est
     * « pas de confidentialité ». À passer aux requêtes paginées plutôt que les seuls centres de
     * rattachement, afin que le filtrage SQL applique les mêmes règles que le service.
     */
    public List<Integer> getVisibleCentreIds(List<CentreGestion> centresDemandeur) {
        return confidentialiteService.getVisibleCentreIds(centresDemandeur, centreGestionJpaRepository.findAll());
    }

    public boolean isAttachedToCentre(List<CentreGestion> centresDemandeur, CentreGestion centreGestion) {
        return centresDemandeur != null
                && centreGestion != null
                && centresDemandeur.stream().anyMatch(centreDemandeur -> centreDemandeur.getId() == centreGestion.getId());
    }

    public boolean canViewContact(List<CentreGestion> centresDemandeur, Contact contact) {
        return centresDemandeur != null
                && centresDemandeur.stream().anyMatch(centreDemandeur -> confidentialiteService.canViewContact(centreDemandeur, contact));
    }

    public boolean canViewService(List<CentreGestion> centresDemandeur, Service service) {
        return centresDemandeur != null
                && centresDemandeur.stream().anyMatch(centreDemandeur -> confidentialiteService.canViewService(centreDemandeur, service));
    }

    public boolean canViewStructureCoordinates(List<CentreGestion> centresDemandeur, Structure structure) {
        return centresDemandeur != null
                && centresDemandeur.stream().anyMatch(centreDemandeur -> confidentialiteService.canViewStructureCoordinates(centreDemandeur, structure));
    }

    public boolean canUseContactForConvention(Contact contact, CentreGestion centreGestionConvention, List<CentreGestion> centresDemandeur) {
        CentreGestion porteur = contact != null ? contact.getCentreGestion() : null;
        return canUseForConvention(porteur, centreGestionConvention, centresDemandeur);
    }

    public boolean canUseServiceForConvention(Service service, CentreGestion centreGestionConvention, List<CentreGestion> centresDemandeur) {
        CentreGestion porteur = service != null ? service.getCentreGestion() : null;
        return canUseForConvention(porteur, centreGestionConvention, centresDemandeur);
    }

    private boolean canUseForConvention(CentreGestion porteur, CentreGestion centreGestionConvention, List<CentreGestion> centresDemandeur) {
        return porteur != null
                && ((centreGestionConvention != null && porteur.getId() == centreGestionConvention.getId())
                || isAttachedToCentre(centresDemandeur, porteur)
                || confidentialiteService.isNoConfidentiality(porteur)
                || confidentialiteService.isCentreEtablissement(porteur));
    }
}
