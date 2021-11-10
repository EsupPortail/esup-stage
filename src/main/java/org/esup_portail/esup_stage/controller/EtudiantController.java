package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.ApogeeMap;
import org.esup_portail.esup_stage.service.apogee.model.EtapeInscription;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/{numEtudiant}/apogee-data")
    @Secure
    public EtudiantRef getApogeeData(@PathVariable("numEtudiant") String numEtudiant) {
        return apogeeService.getInfoApogee(numEtudiant, appConfigService.getAnneeUniv());
    }

    @GetMapping("/{numEtudiant}/apogee-inscriptions")
    @Secure
    public List<EtapeInscription> getFormationInscriptions(@PathVariable("numEtudiant") String numEtudiant) {
        List<String> anneeInscriptions = apogeeService.getAnneeInscriptions(numEtudiant);
        List<EtapeInscription> inscriptions = new ArrayList<>();
        for (String annee : anneeInscriptions) {
            ApogeeMap apogeeMap = apogeeService.getEtudiantEtapesInscription(numEtudiant, annee);
            List<EtapeInscription> inscr = apogeeMap.getListeEtapeInscriptions().stream().peek(i -> i.setAnnee(annee)).collect(Collectors.toList());
            inscriptions.addAll(inscr);
        }
        return inscriptions;
    }
}
