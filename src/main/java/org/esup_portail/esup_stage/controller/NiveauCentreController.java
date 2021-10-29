package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.model.NiveauCentre;
import org.esup_portail.esup_stage.repository.CentreGestionRepository;
import org.esup_portail.esup_stage.repository.NiveauCentreJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@ApiController
@RequestMapping("/niveau-centre")
public class NiveauCentreController {

    @Autowired
    NiveauCentreJpaRepository niveauCentreJpaRepository;

    @Autowired
    CentreGestionRepository centreGestionRepository;

    @GetMapping
    @Secure()
    public List<NiveauCentre> findAll() {
        return niveauCentreJpaRepository.findAll();
    }

    @GetMapping("/centre-gestion-list")
    @Secure()
    public List<NiveauCentre> findList() {
        List<NiveauCentre> list = niveauCentreJpaRepository.findAll();
        if (centreGestionRepository.etablissementExists()) {
            return list.stream().filter(l -> !l.getLibelle().equalsIgnoreCase("ETABLISSEMENT")).collect(Collectors.toList());
        } else {
            return list.stream().filter(l -> l.getLibelle().equalsIgnoreCase("ETABLISSEMENT")).collect(Collectors.toList());
        }
    }
}
