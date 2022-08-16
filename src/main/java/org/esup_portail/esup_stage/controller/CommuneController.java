package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.model.Commune;
import org.esup_portail.esup_stage.repository.CommuneJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiController
@RequestMapping("/commune")
public class CommuneController {

    @Autowired
    CommuneJpaRepository communeJpaRepository;

    @GetMapping("/")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public List<Commune> getCommunes() {
        return communeJpaRepository.findAll();
    }

}
