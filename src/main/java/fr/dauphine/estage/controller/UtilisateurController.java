package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.ContextDto;
import fr.dauphine.estage.model.Utilisateur;
import fr.dauphine.estage.security.ServiceContext;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@ApiController
@RequestMapping("/users")
public class UtilisateurController {

    @GetMapping("/connected")
    @Secure(roles = {})
    public Utilisateur getUserConnected() {
        ContextDto contexteDto = ServiceContext.getServiceContext();
        if (contexteDto != null) {
            return contexteDto.getUtilisateur();
        }
        return null;
    }
}
