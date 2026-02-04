package org.esup_portail.esup_stage.security.permission;

import org.aspectj.lang.reflect.MethodSignature;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.StructureJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StructurePermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private StructureJpaRepository structureJpaRepository;

    @Override
    public boolean hasPermission(Utilisateur user, MethodSignature sig, Object[] args) {
        if (!UtilisateurHelper.isRole(user, Role.ETU)) return false;
        Integer id = (Integer) args[0];
        return structureJpaRepository.isOwner(id, user.getId());
    }
}
