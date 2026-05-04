package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.dto.ConventionFormationDto;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.CritereGestionJpaRepository;
import org.esup_portail.esup_stage.repository.PersonnelCentreGestionJpaRepository;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantDiplomeEtapeResponse;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantDiplomeEtapeSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EtudiantSecurityService {

    @Autowired
    ApogeeService apogeeService;

    @Autowired
    PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;

    @Autowired
    CritereGestionJpaRepository critereGestionJpaRepository;

    public boolean isNotOwnResource(Utilisateur utilisateur, String numEtudiant) {
        return utilisateur.getNumEtudiant() == null || !utilisateur.getNumEtudiant().equals(numEtudiant);
    }

    public boolean isNotOwnResourceLogin(Utilisateur utilisateur, String login) {
        return utilisateur.getNumEtudiant() == null || !utilisateur.getLogin().equals(login);
    }

    public boolean isGestionnaireOrResponsableGestionnaire(Utilisateur utilisateur) {
        return utilisateur != null && (UtilisateurHelper.isRole(utilisateur, Role.GES) || UtilisateurHelper.isRole(utilisateur, Role.RESP_GES));
    }

    public boolean isEtuInCentreGestionUtilisateur(Utilisateur utilisateur, String numEtudiant) {
        if (!isGestionnaireOrResponsableGestionnaire(utilisateur) || numEtudiant == null) {
            return false;
        }

        return isEtuInCentreGestionUtilisateur(utilisateur, numEtudiant, getIdsCentresGestionUtilisateur(utilisateur));
    }

    public boolean isEtuInCentreGestionUtilisateur(Utilisateur utilisateur, String numEtudiant, List<Integer> idsCentresGestionUtilisateur) {
        if (utilisateur == null || numEtudiant == null || idsCentresGestionUtilisateur == null || idsCentresGestionUtilisateur.isEmpty()) {
            return false;
        }

        List<ConventionFormationDto> inscriptions = apogeeService.getInscriptions(utilisateur, numEtudiant, null);
        if (inscriptions == null || inscriptions.isEmpty()) {
            return false;
        }

        return inscriptions.stream()
                .map(ConventionFormationDto::getCentreGestion)
                .filter(Objects::nonNull)
                .anyMatch(centreGestionEtudiant -> idsCentresGestionUtilisateur.contains(centreGestionEtudiant.getId()));
    }

    public List<Integer> getIdsCentresGestionUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getUid() == null) {
            return List.of();
        }

        return personnelCentreGestionJpaRepository.findByUidPersonnel(utilisateur.getUid()).stream()
                .map(PersonnelCentreGestion::getCentreGestion)
                .filter(Objects::nonNull)
                .map(CentreGestion::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<CritereGestion> getCriteresCentresGestionUtilisateur(List<Integer> idsCentresGestionUtilisateur) {
        if (idsCentresGestionUtilisateur == null || idsCentresGestionUtilisateur.isEmpty()) {
            return List.of();
        }

        return idsCentresGestionUtilisateur.stream()
                .flatMap(idCentreGestion -> critereGestionJpaRepository.findByCentreId(idCentreGestion).stream())
                .collect(Collectors.toList());
    }

    public boolean isEtudiantDiplomeEtapeInCentreGestionUtilisateur(EtudiantDiplomeEtapeResponse etudiant, List<CritereGestion> criteresCentresGestionUtilisateur) {
        if (etudiant == null || criteresCentresGestionUtilisateur == null || criteresCentresGestionUtilisateur.isEmpty()) {
            return false;
        }

        return criteresCentresGestionUtilisateur.stream()
                .anyMatch(critere -> {
                    String code = critere.getId().getCode();
                    String version = critere.getId().getCodeVersionEtape();
                    if (version == null || version.isEmpty()) {
                        return equalsIgnoreCase(code, etudiant.getCodeComposante());
                    }
                    return equalsIgnoreCase(code, etudiant.getCodeEtape()) && equalsIgnoreCase(version, etudiant.getVersionEtape());
                });
    }

    public boolean isRechercheDiplomeEtapeInCentreGestionUtilisateur(EtudiantDiplomeEtapeSearch search, List<CritereGestion> criteresCentresGestionUtilisateur) {
        if (search == null || criteresCentresGestionUtilisateur == null || criteresCentresGestionUtilisateur.isEmpty()) {
            return false;
        }

        return criteresCentresGestionUtilisateur.stream()
                .anyMatch(critere -> {
                    String code = critere.getId().getCode();
                    String version = critere.getId().getCodeVersionEtape();
                    if (version == null || version.isEmpty()) {
                        return equalsIgnoreCase(code, search.getCodeComposante());
                    }
                    return equalsIgnoreCase(code, search.getCodeEtape()) && equalsIgnoreCase(version, search.getVersionEtape());
                });
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }
}
