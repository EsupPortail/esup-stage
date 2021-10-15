package fr.dauphine.estage.controller;

import fr.dauphine.estage.model.Effectif;
import fr.dauphine.estage.model.Role;
import fr.dauphine.estage.model.Utilisateur;
import fr.dauphine.estage.model.helper.UtilisateurHelper;
import fr.dauphine.estage.repository.EffectifJpaRepository;
import fr.dauphine.estage.security.ServiceContext;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@ApiController
@RequestMapping("/effectifs")
public class EffectifController {

    @Autowired
    EffectifJpaRepository effectifJpaRepository;

    @GetMapping
    @Secure
    public List<Effectif> getAll(@RequestParam(value = "actif", required = false) boolean onlyActif) {
        Utilisateur utilisateur = ServiceContext.getServiceContext().getUtilisateur();
        if (onlyActif || UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            return effectifJpaRepository.findAllActif();
        }
        return effectifJpaRepository.findAll();
    }
}
