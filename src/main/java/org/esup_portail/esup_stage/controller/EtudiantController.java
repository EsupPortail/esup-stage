package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConventionFormationDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Etudiant;
import org.esup_portail.esup_stage.repository.EtudiantJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.ApogeeMap;
import org.esup_portail.esup_stage.service.apogee.model.ElementPedagogique;
import org.esup_portail.esup_stage.service.apogee.model.EtapeInscription;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
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

    @GetMapping("/{numEtudiant}/apogee-data")
    @Secure
    public EtudiantRef getApogeeData(@PathVariable("numEtudiant") String numEtudiant) {
        return apogeeService.getInfoApogee(numEtudiant, appConfigService.getAnneeUniv());
    }

    @GetMapping("/{numEtudiant}/apogee-inscriptions")
    @Secure
    public List<ConventionFormationDto> getFormationInscriptions(@PathVariable("numEtudiant") String numEtudiant) {
        // TODO récupération des années éligibles (voir avec Claude)
        // cas étudiant : année en cours et éventuellement l'année précédente
        // cas autres : toutes les années dispo
        List<String> anneeInscriptions = apogeeService.getAnneeInscriptions(numEtudiant);
        List<ConventionFormationDto> inscriptions = new ArrayList<>();
        for (String annee : anneeInscriptions) {
            ApogeeMap apogeeMap = apogeeService.getEtudiantEtapesInscription(numEtudiant, annee);
            for (EtapeInscription etapeInscription : apogeeMap.getListeEtapeInscriptions()) {
                ConventionFormationDto conventionFormationDto = new ConventionFormationDto();
                conventionFormationDto.setEtapeInscription(etapeInscription);
                conventionFormationDto.setAnnee(annee);
                inscriptions.add(conventionFormationDto);
            }
            for (ElementPedagogique elementPedagogique : apogeeMap.getListeELPs()) {
                ConventionFormationDto conventionFormationDto = inscriptions.stream().filter(i -> i.getEtapeInscription().getCodeEtp().equals(elementPedagogique.getCodEtp()) && i.getEtapeInscription().getCodVrsVet().equals(elementPedagogique.getCodVrsVet())).findAny().orElse(null);
                if (conventionFormationDto != null) {
                    conventionFormationDto.getElementPedagogiques().add(elementPedagogique);
                }
            }
        }
        return inscriptions;
    }

    @GetMapping("/by-login/{login}")
    @Secure
    public Etudiant getByLogin(@PathVariable("login") String login) {
        Etudiant etudiant = etudiantJpaRepository.findByLogin(login);
        if (etudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Etudiant non trouvé");
        }
        return etudiant;
    }
}
