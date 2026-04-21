package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.dto.RegimeInscriptionDto;
import org.esup_portail.esup_stage.service.apogee.RegimeInscriptionApogeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@ApiController
@RequestMapping("/regIns")
public class RegimeInscriptionApogeeController {

    @Autowired
    RegimeInscriptionApogeeService regimeInscriptionApogeeService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public List<RegimeInscriptionDto> getRegimesInscriptionsList() {
        return regimeInscriptionApogeeService.getRegimesInscriptions();
    }

    @PostMapping("/synchroniser")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION})
    public List<RegimeInscriptionDto> synchroniserRegimesInscriptions() {
        return regimeInscriptionApogeeService.synchroniserDepuisApogee();
    }

}
