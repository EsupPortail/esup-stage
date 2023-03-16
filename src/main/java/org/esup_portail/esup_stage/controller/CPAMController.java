package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.model.CPAM;
import org.esup_portail.esup_stage.repository.CPAMJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@ApiController
@RequestMapping("/cpam")
public class CPAMController {

    @Autowired
    CPAMJpaRepository CPAMJpaRepository;

    @GetMapping("/")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<CPAM> getCPAMs() {
        return CPAMJpaRepository.findAll();
    }

}
