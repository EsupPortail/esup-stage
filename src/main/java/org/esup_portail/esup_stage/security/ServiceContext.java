package org.esup_portail.esup_stage.security;

import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ServiceContext {

    public static Utilisateur getUtilisateur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            CasUserDetailsImpl customUserDetails = (CasUserDetailsImpl) authentication.getPrincipal();
            return customUserDetails.getUtilisateur();
        }
        return null;
    }
}
