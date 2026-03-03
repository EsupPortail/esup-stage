package org.esup_portail.esup_stage.security.permission;

import org.aspectj.lang.reflect.MethodSignature;
import org.esup_portail.esup_stage.model.Utilisateur;

public interface PermissionEvaluator {
    boolean hasPermission(Utilisateur user, MethodSignature sig, Object[] args);
}
