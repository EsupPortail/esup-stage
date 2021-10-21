package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.EtudiantSearchDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiController
@RequestMapping("/etudiants")
public class EtudiantController {

    @Autowired
    LdapService ldapService;

    @Autowired
    ApogeeService apogeeService;

    @Autowired
    AppConfigService appConfigService;

    @PostMapping("/ldap-search")
    @Secure
    public List<LdapUser> getEtudiants(@RequestBody EtudiantSearchDto etudiantSearchDto) {
        if (etudiantSearchDto.getId() == null && etudiantSearchDto.getNom() == null && etudiantSearchDto.getPrenom() == null && etudiantSearchDto.getMail() == null && etudiantSearchDto.getPrimaryAffiliation() == null && etudiantSearchDto.getAffiliation() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Veuillez renseigner au moins un des filtres");
        }
        return ldapService.search(etudiantSearchDto);
    }

    @GetMapping("/{numEtudiant}/apogee-data")
    @Secure
    public EtudiantRef getApogeeData(@PathVariable("numEtudiant") String numEtudiant) {
        return apogeeService.getInfoApogee(numEtudiant, appConfigService.getAnneeUniv());
    }
}
