package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.model.NiveauCentre;
import org.esup_portail.esup_stage.repository.NiveauCentreJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@ApiController
@RequestMapping("/niveau-centre")
public class NiveauCentreController {

    @Autowired
    NiveauCentreJpaRepository niveauCentreJpaRepository;

    @GetMapping
    @Secure()
    public List<NiveauCentre> findAll() {
        return niveauCentreJpaRepository.findAll();
    }
}
