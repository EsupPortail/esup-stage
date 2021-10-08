package fr.dauphine.estage.controller;

import fr.dauphine.estage.model.NiveauCentre;
import fr.dauphine.estage.repository.NiveauCentreJpaRepository;
import fr.dauphine.estage.security.interceptor.Secure;
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
