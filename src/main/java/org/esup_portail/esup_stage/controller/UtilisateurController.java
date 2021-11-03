package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ContextDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.UtilisateurJpaRepository;
import org.esup_portail.esup_stage.repository.UtilisateurRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;


@ApiController
@RequestMapping("/users")
public class UtilisateurController {

    @Autowired
    UtilisateurRepository utilisateurRepository;

    @Autowired
    UtilisateurJpaRepository utilisateurJpaRepository;

    @Autowired
    MailerService mailerService;

    @GetMapping("/connected")
    @Secure()
    public Utilisateur getUserConnected() {
        ContextDto contexteDto = ServiceContext.getServiceContext();
        if (contexteDto != null) {
            return contexteDto.getUtilisateur();
        }
        return null;
    }

    @GetMapping
    @Secure(fonction = AppFonctionEnum.PARAM_GLOBAL, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Utilisateur> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Utilisateur> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(utilisateurRepository.count(filters));
        paginatedResponse.setData(utilisateurRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        mailerService.sendMail("sophie.sound@yahoo.fr", "test mail esup", "Test d'envoi d'un mail sur le projet esup stage");
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.PARAM_GLOBAL, droits = {DroitEnum.MODIFICATION})
    public Utilisateur update(@PathVariable("id") int id, @RequestBody Utilisateur requestUtilisateur) {
        Utilisateur utilisateur = utilisateurJpaRepository.findByIdActif(id);
        if (utilisateur == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé");
        }
        utilisateur.setNom(requestUtilisateur.getNom());
        utilisateur.setPrenom(requestUtilisateur.getPrenom());
        utilisateur.setRoles(requestUtilisateur.getRoles());
        if (requestUtilisateur.isActif() != null) {
            utilisateur.setActif(requestUtilisateur.isActif());
        }
        utilisateur = utilisateurJpaRepository.saveAndFlush(utilisateur);
        return utilisateur;
    }
}
