package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.UtilisateurCentreGestionRole;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.UtilisateurCentreGestionRoleJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class HabilitationService {

    @Autowired
    private UtilisateurCentreGestionRoleJpaRepository utilisateurCentreGestionRoleJpaRepository;

    public boolean hasRight(Utilisateur utilisateur, AppFonctionEnum[] fonctions, DroitEnum[] droits)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return hasGlobalRight(utilisateur, fonctions, droits)
                || (isCentreScopedFunction(fonctions) && hasAnyCentreRight(utilisateur, fonctions, droits));
    }

    private boolean isCentreScopedFunction(AppFonctionEnum[] fonctions) {
        return fonctions != null && Arrays.stream(fonctions).anyMatch(fonction ->
                AppFonctionEnum.PARAM_CENTRE.equals(fonction) || AppFonctionEnum.CONVENTION.equals(fonction));
    }

    public boolean hasGlobalRight(Utilisateur utilisateur, AppFonctionEnum[] fonctions, DroitEnum[] droits)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return UtilisateurHelper.isRole(utilisateur, fonctions, droits);
    }

    public boolean hasCentreRight(Utilisateur utilisateur, int idCentreGestion, AppFonctionEnum[] fonctions, DroitEnum[] droits)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (utilisateur == null) {
            return false;
        }
        List<Role> roles = utilisateurCentreGestionRoleJpaRepository
                .findByUtilisateurIdAndCentreGestionId(utilisateur.getId(), idCentreGestion)
                .stream()
                .map(UtilisateurCentreGestionRole::getRole)
                .toList();
        return UtilisateurHelper.isRole(roles, fonctions, droits);
    }

    public boolean hasAnyCentreRight(Utilisateur utilisateur, AppFonctionEnum[] fonctions, DroitEnum[] droits)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (utilisateur == null) {
            return false;
        }
        List<Role> roles = utilisateurCentreGestionRoleJpaRepository.findByUtilisateurId(utilisateur.getId())
                .stream()
                .map(UtilisateurCentreGestionRole::getRole)
                .toList();
        return UtilisateurHelper.isRole(roles, fonctions, droits);
    }

    public List<Integer> getAuthorizedCentreIds(Utilisateur utilisateur, AppFonctionEnum[] fonctions, DroitEnum[] droits) {
        if (utilisateur == null) {
            return Collections.emptyList();
        }
        return utilisateurCentreGestionRoleJpaRepository.findByUtilisateurId(utilisateur.getId())
                .stream()
                .filter(userCentreRole -> hasRoleRight(userCentreRole.getRole(), fonctions, droits))
                .map(userCentreRole -> userCentreRole.getCentreGestion().getId())
                .distinct()
                .toList();
    }

    public List<Role> getRolesByUtilisateurAndCentre(int idUtilisateur, int idCentreGestion) {
        return utilisateurCentreGestionRoleJpaRepository.findByUtilisateurIdAndCentreGestionId(idUtilisateur, idCentreGestion)
                .stream()
                .map(UtilisateurCentreGestionRole::getRole)
                .toList();
    }

    /**
     * Retourne les rôles qui font foi pour l'utilisateur sur un centre de gestion donné :
     * les rôles définis spécifiquement sur ce centre de gestion s'ils existent (ils priment
     * alors sur le rôle global), sinon les rôles globaux de l'utilisateur.
     */
    public List<Role> getEffectiveRoles(Utilisateur utilisateur, Integer idCentreGestion) {
        if (utilisateur == null) {
            return Collections.emptyList();
        }
        if (idCentreGestion != null) {
            List<Role> centreRoles = getRolesByUtilisateurAndCentre(utilisateur.getId(), idCentreGestion);
            if (!centreRoles.isEmpty()) {
                return centreRoles;
            }
        }
        return utilisateur.getRoles();
    }

    public boolean hasCentreRoles(Utilisateur utilisateur, Integer idCentreGestion) {
        if (utilisateur == null || idCentreGestion == null) {
            return false;
        }
        return !getRolesByUtilisateurAndCentre(utilisateur.getId(), idCentreGestion).isEmpty();
    }

    /**
     * Vrai si l'utilisateur est gestionnaire ou responsable gestionnaire au niveau global
     * ou sur au moins un centre de gestion (rôle appliqué sur le centre).
     */
    public boolean isGestionnaire(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return false;
        }
        if (UtilisateurHelper.isRole(utilisateur, Role.GES) || UtilisateurHelper.isRole(utilisateur, Role.RESP_GES)) {
            return true;
        }
        return !getGestionnaireCentreIds(utilisateur).isEmpty();
    }

    /**
     * Identifiants des centres de gestion sur lesquels l'utilisateur possède un rôle
     * gestionnaire ou responsable gestionnaire.
     */
    public List<Integer> getGestionnaireCentreIds(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return Collections.emptyList();
        }
        return utilisateurCentreGestionRoleJpaRepository.findByUtilisateurId(utilisateur.getId())
                .stream()
                .filter(userCentreRole -> {
                    Role role = userCentreRole.getRole();
                    return role != null && (Role.GES.equals(role.getCode()) || Role.RESP_GES.equals(role.getCode()));
                })
                .map(userCentreRole -> userCentreRole.getCentreGestion().getId())
                .distinct()
                .toList();
    }

    public List<Role> getRolesByUidAndCentre(String uid, int idCentreGestion) {
        if (uid == null || uid.isBlank()) {
            return Collections.emptyList();
        }
        return utilisateurCentreGestionRoleJpaRepository.findByUtilisateurUidAndCentreGestionId(uid, idCentreGestion)
                .stream()
                .map(UtilisateurCentreGestionRole::getRole)
                .toList();
    }

    public void replaceCentreRoles(Utilisateur utilisateur, int idCentreGestion, Collection<Role> roles) {
        utilisateurCentreGestionRoleJpaRepository.deleteByUtilisateurIdAndCentreGestionId(utilisateur.getId(), idCentreGestion);
        if (roles == null || roles.isEmpty()) {
            return;
        }
        roles.stream()
                .filter(role -> role != null && role.getId() != 0)
                .distinct()
                .forEach(role -> {
                    UtilisateurCentreGestionRole userCentreRole = new UtilisateurCentreGestionRole();
                    userCentreRole.setUtilisateur(utilisateur);
                    userCentreRole.setRole(role);
                    org.esup_portail.esup_stage.model.CentreGestion centreGestion = new org.esup_portail.esup_stage.model.CentreGestion();
                    centreGestion.setId(idCentreGestion);
                    userCentreRole.setCentreGestion(centreGestion);
                    utilisateurCentreGestionRoleJpaRepository.save(userCentreRole);
                });
        utilisateurCentreGestionRoleJpaRepository.flush();
    }

    private boolean hasRoleRight(Role role, AppFonctionEnum[] fonctions, DroitEnum[] droits) {
        try {
            return UtilisateurHelper.isRole(List.of(role), fonctions, droits);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return false;
        }
    }
}
