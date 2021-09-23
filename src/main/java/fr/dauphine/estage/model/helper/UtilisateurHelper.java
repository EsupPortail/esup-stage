package fr.dauphine.estage.model.helper;

import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.enums.RoleEnum;
import fr.dauphine.estage.model.Role;
import fr.dauphine.estage.model.RoleAppFonction;
import fr.dauphine.estage.model.Utilisateur;

import java.lang.reflect.InvocationTargetException;

public class UtilisateurHelper {
    public static boolean isAdmin(Utilisateur utilisateur) {
        return isRole(utilisateur, RoleEnum.ADM);
    }

    public static boolean isRole(Utilisateur utilisateur, RoleEnum code) {
        return utilisateur.getRoles().stream().anyMatch(r -> r.getCode() == code);
    }

    public static boolean isRole(Utilisateur utilisateur, AppFonctionEnum fonction, DroitEnum[] droits) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        boolean hasRight = false;
        for (Role role : utilisateur.getRoles()) {
            for (RoleAppFonction habilitation : role.getRoleAppFonctions()) {
                if (habilitation.getAppFonction().getCode() == fonction) {
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
