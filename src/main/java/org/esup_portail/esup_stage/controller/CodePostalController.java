package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.model.CodePostal;
import org.esup_portail.esup_stage.repository.CodePostalJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@ApiController
@RequestMapping("/codePostal")
public class CodePostalController {

    @Autowired
    CodePostalJpaRepository codePostalJpaRepository;

    @GetMapping("/")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public List<CodePostal> getCodePostals() {
        return codePostalJpaRepository.findAll();
    }

}
