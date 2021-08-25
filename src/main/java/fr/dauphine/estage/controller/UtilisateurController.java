package fr.dauphine.estage.controller;

import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@ApiController
@RequestMapping("/users")
public class UtilisateurController {

    @GetMapping("/connected")
    @Secure(roles = {})
    public Object getUserConnected(HttpServletRequest request) {
        return request.getAttribute("utilisateur");
    }
}
