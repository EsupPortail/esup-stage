package org.esup_portail.esup_stage.security.permission;

import org.aspectj.lang.reflect.MethodSignature;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.ContactJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContactPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private ContactJpaRepository contactJpaRepository;

    @Autowired
    private CentreGestionJpaRepository centreGestionJpaRepository;

    @Override
    public boolean hasPermission(Utilisateur user, MethodSignature sig, Object[] args) {
        Integer id = (Integer) args[0];

        if (UtilisateurHelper.isRole(user, Role.ETU)) {
            return contactJpaRepository.isOwner(id, user.getId());
        }

        if (UtilisateurHelper.isRole(user, Role.GES) || UtilisateurHelper.isRole(user, Role.RESP_GES)) {
            if (user == null || user.getUid() == null || user.getUid().isBlank()) {
                return false;
            }
            List<Integer> centreIds = centreGestionJpaRepository.findAllByGestionnaireUid(user.getUid()).stream()
                    .map(CentreGestion::getId)
                    .toList();
            return !centreIds.isEmpty() && contactJpaRepository.existsByIdAndCentreGestionIdIn(id, centreIds);
        }

        return true;
    }
}