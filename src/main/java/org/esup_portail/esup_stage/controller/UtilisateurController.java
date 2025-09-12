package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.PersonneDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.RoleJpaRepository;
import org.esup_portail.esup_stage.repository.UtilisateurJpaRepository;
import org.esup_portail.esup_stage.repository.UtilisateurRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@ApiController
@RequestMapping("/users")
public class UtilisateurController {

    @Autowired
    UtilisateurRepository utilisateurRepository;

    @Autowired
    UtilisateurJpaRepository utilisateurJpaRepository;

    @Autowired
    RoleJpaRepository roleJpaRepository;

    @GetMapping("/connected")
    @Secure()
    public Utilisateur getUserConnected() {
        return ServiceContext.getUtilisateur();
    }

    @GetMapping("/{login}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public Utilisateur findOneByLogin(@PathVariable("login") String login) {
        return utilisateurJpaRepository.findOneByLogin(login);
    }

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Utilisateur> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Utilisateur> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(utilisateurRepository.count(filters));
        paginatedResponse.setData(utilisateurRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        byte[] bytes = utilisateurRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        StringBuilder csv = utilisateurRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public Utilisateur update(@PathVariable("id") int id, @RequestBody Utilisateur requestUtilisateur) {
        Utilisateur utilisateur = utilisateurJpaRepository.findById(id);
        if (utilisateur == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé");
        }
        utilisateur.setNom(requestUtilisateur.getNom());
        utilisateur.setPrenom(requestUtilisateur.getPrenom());
        List<Role> dbRoles = new ArrayList<>();
        for (Role role : requestUtilisateur.getRoles()) {
            dbRoles.add(roleJpaRepository.findById(role.getId()));
        }
        utilisateur.setRoles(dbRoles);
        if (requestUtilisateur.getActif() != null) {
            utilisateur.setActif(requestUtilisateur.getActif());
        }
        utilisateur = utilisateurJpaRepository.saveAndFlush(utilisateur);
        return utilisateur;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.CREATION})
    public Utilisateur create(@RequestBody Utilisateur requestUtilisateur) {
        Utilisateur utilisateur = utilisateurJpaRepository.findOneByLogin(requestUtilisateur.getLogin());
        if (utilisateur != null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Utilisateur déjà existant");
        }
        if (requestUtilisateur.getActif() == null) {
            requestUtilisateur.setActif(false);
        }
        List<Role> dbRoles = new ArrayList<>();
        for (Role role : requestUtilisateur.getRoles()) {
            dbRoles.add(roleJpaRepository.findById(role.getId()));
        }
        requestUtilisateur.setRoles(dbRoles);
        requestUtilisateur = utilisateurJpaRepository.saveAndFlush(requestUtilisateur);
        return requestUtilisateur;
    }

    @GetMapping("/personne/{login}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public PersonneDto getPersonneByLogin(@PathVariable("login") String login) {
        PersonneDto personneDto = new PersonneDto();
        Utilisateur utilisateur = utilisateurJpaRepository.findOneByLogin(login);
        if(utilisateur == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé");
        }
        personneDto.setNom(utilisateur.getNom());
        personneDto.setPrenom(utilisateur.getPrenom());
        return personneDto;
    }
}
