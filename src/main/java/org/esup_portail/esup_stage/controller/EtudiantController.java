package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.dto.ConventionFormationDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.EtudiantSecurityService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantDiplomeEtapeResponse;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantDiplomeEtapeSearch;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

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
    EtudiantSecurityService etudiantSecurityService;

    Logger logger = Logger.getLogger(String.valueOf(EtudiantController.class));

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.CREATION},forbiddenEtu = true)
    public PaginatedResponse<Etudiant> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Etudiant> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(etudiantRepository.count(filters));
        paginatedResponse.setData(etudiantRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{numEtudiant}/apogee-data")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.CREATION})
    public EtudiantRef getApogeeData(@PathVariable("numEtudiant") String numEtudiant) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if(utilisateur != null && UtilisateurHelper.isRole(utilisateur, Role.ETU) && etudiantSecurityService.isNotOwnResource(utilisateur,numEtudiant)){
            logger.warning("Accès refusé à l'utilisateur " + utilisateur.getLogin() + " pour les données sur le numero étudiant " + numEtudiant);
            throw new AppException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        Etudiant etudiant = etudiantRepository.findByNumEtudiant(numEtudiant);
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && (etudiant == null || !utilisateur.getUid().equals(etudiant.getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "Étudiant non trouvé");
        }
        if(etudiantSecurityService.isGestionnaireOrResponsableGestionnaire(utilisateur)){
            if(!etudiantSecurityService.isEtuInCentreGestionUtilisateur(utilisateur,numEtudiant)){
                logger.warning("Accès refusé à l'utilisateur " + utilisateur.getLogin() + " pour les données sur le numero étudiant " + numEtudiant);
                throw new AppException(HttpStatus.FORBIDDEN, "Accès refusé");
            }
        }
        if(UtilisateurHelper.isRole(utilisateur, Role.ENS)){
            logger.warning("Accès refusé à l'utilisateur " + utilisateur.getLogin() + " pour les données sur le numero étudiant " + numEtudiant);
            throw new AppException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        return apogeeService.getInfoApogee(numEtudiant, appConfigService.getAnneeUniv());
    }

    @GetMapping("/{numEtudiant}/apogee-inscriptions")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.CREATION})
    public List<ConventionFormationDto> getFormationInscriptions(@PathVariable("numEtudiant") String numEtudiant, @RequestParam(name = "annee", required = false) String annee) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if(utilisateur != null && UtilisateurHelper.isRole(utilisateur, Role.ETU) && etudiantSecurityService.isNotOwnResource(utilisateur,numEtudiant)){
            logger.warning("Accès refusé à l'utilisateur " + utilisateur.getLogin() + " pour les données sur le numero étudiant " + numEtudiant);
            throw new AppException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !numEtudiant.equals(utilisateur.getNumEtudiant())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Étudiant non trouvé");
        }
        if(etudiantSecurityService.isGestionnaireOrResponsableGestionnaire(utilisateur)){
            if(!etudiantSecurityService.isEtuInCentreGestionUtilisateur(utilisateur,numEtudiant)){
                logger.warning("Accès refusé à l'utilisateur " + utilisateur.getLogin() + " pour les données sur le numero étudiant " + numEtudiant);
                throw new AppException(HttpStatus.FORBIDDEN, "Accès refusé");
            }
        }
        if(UtilisateurHelper.isRole(utilisateur, Role.ENS)){
            logger.warning("Accès refusé à l'utilisateur " + utilisateur.getLogin() + " pour les données sur le numero étudiant " + numEtudiant);
            throw new AppException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        return apogeeService.getInscriptions(utilisateur, numEtudiant, annee);
    }

    @GetMapping("/by-login/{login}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.CREATION})
    public Etudiant getByLogin(@PathVariable("login") String login) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if(UtilisateurHelper.isRole(utilisateur, Role.GES) || UtilisateurHelper.isRole(utilisateur, Role.RESP_GES)||UtilisateurHelper.isRole(utilisateur, Role.ENS)){
            logger.warning("Accès refusé à l'utilisateur " + utilisateur.getLogin() + " pour les données sur le login étudiant " + login);
            throw new AppException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        if(utilisateur != null && UtilisateurHelper.isRole(utilisateur, Role.ETU) && etudiantSecurityService.isNotOwnResourceLogin(utilisateur,login)){
            logger.warning("Accès refusé à l'utilisateur " + utilisateur.getLogin() + " pour les données sur le login étudiant " + login);
            throw new AppException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        Etudiant etudiant = etudiantJpaRepository.findByLogin(login);
        if (etudiant == null || UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(etudiant.getIdentEtudiant())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Etudiant non trouvé");
        }
        return etudiant;
    }

    @PostMapping("/diplome-etape")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE},forbiddenEtu = true)
    public EtudiantDiplomeEtapeResponse[] getLdapUsers(@RequestBody EtudiantDiplomeEtapeSearch search) {
        EtudiantDiplomeEtapeResponse[] etudiants = apogeeService.getEtudiantsParDiplomeEtape(search);
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (etudiantSecurityService.isGestionnaireOrResponsableGestionnaire(utilisateur)) {
            List<CritereGestion> criteresCentresGestionUtilisateur = etudiantSecurityService.getCriteresCentresGestionUtilisateur(etudiantSecurityService.getIdsCentresGestionUtilisateur(utilisateur));
            return Arrays.stream(etudiants)
                    .filter(etudiant -> etudiantSecurityService.isEtudiantDiplomeEtapeInCentreGestionUtilisateur(etudiant, criteresCentresGestionUtilisateur))
                    .toArray(EtudiantDiplomeEtapeResponse[]::new);
        }

        return etudiants;
    }
}
