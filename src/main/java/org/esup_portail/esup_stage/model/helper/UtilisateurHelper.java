package org.esup_portail.esup_stage.model.helper;

import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.RoleAppFonction;
import org.esup_portail.esup_stage.model.Utilisateur;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class UtilisateurHelper {
    public static boolean isAdmin(Utilisateur utilisateur) {
        return isRole(utilisateur, Role.ADM);
    }

    public static boolean isRole(Utilisateur utilisateur, String code) {
        return utilisateur.getRoles().stream().anyMatch(r -> r.getCode().equals(code) || (code != null && code.equals(r.getOrigine())));
    }

    public static boolean isRole(Utilisateur utilisateur, AppFonctionEnum[] fonctions, DroitEnum[] droits) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        boolean hasRight = false;
        for (Role role : utilisateur.getRoles()) {
            for (RoleAppFonction habilitation : role.getRoleAppFonctions()) {
                if (Arrays.stream(fonctions).anyMatch(f -> f == habilitation.getAppFonction().getCode())) {
                    for (DroitEnum droit : droits) {
                        String method = "is" + droit.toString().substring(0, 1).toUpperCase() + droit.toString().substring(1).toLowerCase();
                        if ((boolean) RoleAppFonction.class.getMethod(method).invoke(habilitation)) {
                            hasRight = true;
                            break;
                        }
                    }
                }
            }
        }
        return hasRight;
    }
}
