package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

@ApiController
@RequestMapping("/ldap")
public class LdapController {

    @Autowired
    LdapService ldapService;

    @PostMapping("/etudiants")
    @Secure(forbiddenEtu = true)
    public List<LdapUser> getLdapUsers(@RequestBody LdapSearchDto ldapSearchDto) {
        if (ldapSearchDto.getCodEtu() == null && ldapSearchDto.getNom() == null && ldapSearchDto.getPrenom() == null && ldapSearchDto.getMail() == null && ldapSearchDto.getPrimaryAffiliation() == null &&
            ldapSearchDto.getAffiliation() == null && ldapSearchDto.getSupannEntiteAffectation() == null && ldapSearchDto.getSupannEtuEtape() == null && ldapSearchDto.getSupannEtuAnneeInscription() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Veuillez renseigner au moins un des filtres");
        }
        return ldapService.search("/etudiant", ldapSearchDto);
    }

    @PostMapping("/enseignants")
    @Secure
    public List<LdapUser> getLdapEnseignants(@RequestBody LdapSearchDto ldapSearchDto) {
        if (ldapSearchDto.getId() == null && ldapSearchDto.getNom() == null && ldapSearchDto.getPrenom() == null && ldapSearchDto.getMail() == null && ldapSearchDto.getPrimaryAffiliation() == null && ldapSearchDto.getAffiliation() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Veuillez renseigner au moins un des filtres");
        }
        return ldapService.search("/tuteur", ldapSearchDto);
    }

    @PostMapping("/search-by-name")
    @Secure(forbiddenEtu = true)
    public List<LdapUser> searchLdapUserByName(@RequestBody LdapSearchDto ldapSearchDto) {
        if (ldapSearchDto.getNom() == null && ldapSearchDto.getPrenom() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Veuillez renseigner au moins un des filtres");
        }
        return ldapService.search("/staff", ldapSearchDto);
    }

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public List<LdapUser> searchLdapUserByLogin(@Valid @RequestParam("login") @Pattern(regexp = "[A-Za-z0-9]+") String login) {
        List<LdapUser> response = new ArrayList<>();
        LdapUser ldapUser = ldapService.searchByLogin(login);
        if (ldapUser != null) {
            response.add(ldapUser);
        }
        return response;
    }

}
