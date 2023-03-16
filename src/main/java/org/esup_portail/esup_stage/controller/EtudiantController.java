package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConventionFormationDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@ApiController
@RequestMapping("/etudiants")
public class EtudiantController {

    @Autowired
    ApogeeService apogeeService;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    EtudiantRepository etudiantRepository;

    @Autowired
    EtudiantJpaRepository etudiantJpaRepository;

    @Autowired
    CritereGestionJpaRepository critereGestionJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    TypeConventionJpaRepository typeConventionJpaRepository;

    @Autowired
    EtapeJpaRepository etapeJpaRepository;

    @GetMapping
    @Secure
    public PaginatedResponse<Etudiant> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {

        PaginatedResponse<Etudiant> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(etudiantRepository.count(filters));
        paginatedResponse.setData(etudiantRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{numEtudiant}/apogee-data")
    @Secure
    public EtudiantRef getApogeeData(@PathVariable("numEtudiant") String numEtudiant) {
        Etudiant etudiant = etudiantRepository.findByNumEtudiant(numEtudiant);
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && (etudiant == null || !utilisateur.getUid().equals(etudiant.getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "Étudiant non trouvé");
        }
        return apogeeService.getInfoApogee(numEtudiant, appConfigService.getAnneeUniv());
    }

    @GetMapping("/{numEtudiant}/apogee-inscriptions")
    @Secure
    public List<ConventionFormationDto> getFormationInscriptions(@PathVariable("numEtudiant") String numEtudiant, @RequestParam(name = "annee", required = false) String annee) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !numEtudiant.equals(utilisateur.getNumEtudiant())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Étudiant non trouvé");
        }
        return apogeeService.getInscriptions(utilisateur, numEtudiant, annee);
    }

    @GetMapping("/by-login/{login}")
    @Secure
    public Etudiant getByLogin(@PathVariable("login") String login) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        Etudiant etudiant = etudiantJpaRepository.findByLogin(login);
        if (etudiant == null || UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(etudiant.getIdentEtudiant())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Etudiant non trouvé");
        }
        return etudiant;
    }
}
