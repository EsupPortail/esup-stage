package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.EtudiantSearchDto;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.security.interceptor.Secure;
import fr.dauphine.estage.service.ldap.LdapService;
import fr.dauphine.estage.service.ldap.model.LdapUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@ApiController
@RequestMapping("/etudiants")
public class EtudiantController {

    @Autowired
    LdapService ldapService;

    @PostMapping("/ldap-search")
    @Secure
    public List<LdapUser> getEtudiants(@RequestBody EtudiantSearchDto etudiantSearchDto) {
        if (etudiantSearchDto.getId() == null && etudiantSearchDto.getNom() == null && etudiantSearchDto.getPrenom() == null && etudiantSearchDto.getMail() == null && etudiantSearchDto.getPrimaryAffiliation() == null && etudiantSearchDto.getAffiliation() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Veuillez renseigner au moins un des filtres");
        }
        return ldapService.search(etudiantSearchDto);
    }
}
