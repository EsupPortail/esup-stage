package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ContextDto;
import org.esup_portail.esup_stage.dto.ConventionFormationDto;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ApiController
@RequestMapping("/etudiants")
public class EtudiantController {

    @Autowired
    ApogeeService apogeeService;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    EtudiantJpaRepository etudiantJpaRepository;

    @Autowired
    EtudiantRepository etudiantRepository;

    @Autowired
    CritereGestionJpaRepository critereGestionJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    TypeConventionJpaRepository typeConventionJpaRepository;

    @Autowired
    EtapeJpaRepository etapeJpaRepository;

    @GetMapping("/{numEtudiant}/apogee-data")
    @Secure
    public EtudiantRef getApogeeData(@PathVariable("numEtudiant") String numEtudiant) {
        Etudiant etudiant = etudiantRepository.findByNumEtudiant(numEtudiant);
        Utilisateur utilisateur = ServiceContext.getServiceContext().getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && (etudiant == null || !utilisateur.getLogin().equals(etudiant.getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "Étudiant non trouvé");
        }
        return apogeeService.getInfoApogee(numEtudiant, appConfigService.getAnneeUniv());
    }

    @GetMapping("/{numEtudiant}/apogee-inscriptions")
    @Secure
    public List<ConventionFormationDto> getFormationInscriptions(@PathVariable("numEtudiant") String numEtudiant) {
        ContextDto contextDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contextDto.getUtilisateur();
        Etudiant etudiant = etudiantRepository.findByNumEtudiant(numEtudiant);
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && (etudiant == null || !utilisateur.getLogin().equals(etudiant.getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "Étudiant non trouvé");
        }
        List<String> annees = new ArrayList<>();
        String anneeEnCours = appConfigService.getAnneeUniv();
        String anneePrecedente = String.valueOf(Integer.parseInt(anneeEnCours) - 1);
        annees.add(anneePrecedente); // Ajout de l'année précédente
        annees.add(anneeEnCours); // Ajout de l'année en cours
        Date currentDate = new Date();
        Calendar dateBascule = appConfigService.getDateBascule(Integer.parseInt(anneeEnCours));
        List<ConventionFormationDto> inscriptions = new ArrayList<>();
        List<String> anneeInscriptions = apogeeService.getAnneeInscriptions(numEtudiant);
        for (String annee : anneeInscriptions) {
            ApogeeMap apogeeMap = apogeeService.getEtudiantEtapesInscription(numEtudiant, annee);
            RegimeInscription regIns = apogeeMap.getRegimeInscription().stream().filter(r -> r.getAnnee().equals(annee)).findAny().orElse(null);
            TypeConvention typeConvention = null;
            if (regIns != null) {
                typeConvention = typeConventionJpaRepository.findByCodeCtrl(regIns.getLicRegIns());
            }
            for (EtapeInscription etapeInscription : apogeeMap.getListeEtapeInscriptions()) {
                Etape etape = etapeJpaRepository.findById(etapeInscription.getCodeEtp(), etapeInscription.getCodVrsVet(), appConfigService.getConfigGenerale().getCodeUniversite());

                // alimentation de la table Etape avec celles remontées depuis Apogée
                if (etape == null) {
                    EtapeId etapeId = new EtapeId();
                    etapeId.setCode(etapeInscription.getCodeEtp());
                    etapeId.setCodeVersionEtape(etapeInscription.getCodVrsVet());
                    etapeId.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());

                    etape = new Etape();
                    etape.setId(etapeId);
                    etape.setLibelle(etapeInscription.getLibWebVet());
                    etapeJpaRepository.saveAndFlush(etape);
                }
                ConventionFormationDto conventionFormationDto = new ConventionFormationDto();
                conventionFormationDto.setEtapeInscription(etapeInscription);
                conventionFormationDto.setAnnee(annee);
                conventionFormationDto.setTypeConvention(typeConvention);
                CentreGestion centreGestion = null;
                // Recherche du centre de gestion par codeEtape/versionEtape
                CritereGestion critereGestion = critereGestionJpaRepository.findEtapeById(etapeInscription.getCodeEtp(), etapeInscription.getCodVrsVet());
                // Si non trouvé, recherche par code composante et version = ""
                if (critereGestion == null) {
                    critereGestion = critereGestionJpaRepository.findEtapeById(etapeInscription.getCodeComposante(), "");
                }
                // Si non trouvé on vérifie l'autorisation de création de convention non liée à un centre
                if (critereGestion == null) {
                    // récupération du centre de gestion établissement si autorisation de création d'une convention non rattachée à un centre
                    if (appConfigService.getConfigGenerale().isAutoriserConventionsOrphelines()) {
                        centreGestion = centreGestionJpaRepository.getCentreEtablissement();
                    }
                } else {
                    centreGestion = critereGestion.getCentreGestion();
                }
                if (centreGestion != null) {
                    conventionFormationDto.setCentreGestion(centreGestion);
                    inscriptions.add(conventionFormationDto);
                }
            }
            for (ElementPedagogique elementPedagogique : apogeeMap.getListeELPs()) {
                ConventionFormationDto conventionFormationDto = inscriptions.stream().filter(i -> i.getEtapeInscription().getCodeEtp().equals(elementPedagogique.getCodEtp()) && i.getEtapeInscription().getCodVrsVet().equals(elementPedagogique.getCodVrsVet())).findAny().orElse(null);
                if (conventionFormationDto != null) {
                    conventionFormationDto.getElementPedagogiques().add(elementPedagogique);
                }
            }
        }
        // On supprime les formations sans ELP si la config n'autorise pas la création de convention sans ELP
        if (!appConfigService.getConfigGenerale().isAutoriserElementPedagogiqueFacultatif()) {
            inscriptions = inscriptions.stream().filter(i -> i.getElementPedagogiques().size() > 0).collect(Collectors.toList());
        }
        if (!UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                // On garde les formations dont le centre de gestion autorise la création d'une convention
                inscriptions = inscriptions.stream().filter(i -> i.getCentreGestion().isAutorisationEtudiantCreationConvention()).collect(Collectors.toList());
            }
            // Si ce n'est pas un utilisateur admin, on doit afficher les formations de l'année précédente seulement si le centre l'autorise
            inscriptions = inscriptions.stream().filter(i -> {
                CentreGestion centreGestion = i.getCentreGestion();
                Boolean autorisationAnneePrecedente = centreGestion.getRecupInscriptionAnterieure();
                // On autorise la création de convention sur l'année en cours et les années suivantes
                int anneeEnCoursInt = Integer.parseInt(anneeEnCours);
                int anneeInt = Integer.parseInt(i.getAnnee());
                if (i.getAnnee().equals(anneeEnCours) || anneeInt > anneeEnCoursInt) {
                    return true;
                }
                if (!autorisationAnneePrecedente) {
                    return false;
                } else {
                    // On autorise uniquement les gestionnaires pour l'année précédentes (et pas toutes les années précédentes)
                    if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                        return false;
                    } else {
                        return (anneeEnCoursInt - 1) == anneeInt;
                    }
                }
            }).collect(Collectors.toList());
        }
        return inscriptions;
    }

    @GetMapping("/by-login/{login}")
    @Secure
    public Etudiant getByLogin(@PathVariable("login") String login) {
        Utilisateur utilisateur = ServiceContext.getServiceContext().getUtilisateur();
        Etudiant etudiant = etudiantJpaRepository.findByLogin(login);
        if (etudiant == null || UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getLogin().equals(etudiant.getIdentEtudiant())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Etudiant non trouvé");
        }
        return etudiant;
    }
}
