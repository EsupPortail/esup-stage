package fr.dauphine.estage.model.helper;

import fr.dauphine.estage.model.RoleEnum;
import fr.dauphine.estage.model.Utilisateur;

public class UtilisateurHelper {
    public static boolean isAdmin(Utilisateur utilisateur) {
        return isRole(utilisateur, RoleEnum.ADM);
    }

    public static boolean isRole(Utilisateur utilisateur, RoleEnum code) {
        return utilisateur.getRoles().stream().anyMatch(r -> r.getCode() == code);
    }
}
