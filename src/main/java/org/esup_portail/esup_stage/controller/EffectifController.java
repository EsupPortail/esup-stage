package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.model.Effectif;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.EffectifJpaRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
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
