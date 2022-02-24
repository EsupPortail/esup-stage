package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.model.NiveauCentre;
import org.esup_portail.esup_stage.repository.CentreGestionRepository;
import org.esup_portail.esup_stage.repository.NiveauCentreJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApiController
@RequestMapping("/niveau-centre")
public class NiveauCentreController {

    @Autowired
    NiveauCentreJpaRepository niveauCentreJpaRepository;

    @Autowired
    CentreGestionRepository centreGestionRepository;

    @Autowired
    AppConfigService appConfigService;

    @GetMapping("/centre-gestion-list")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public List<NiveauCentre> findList() {
        ConfigGeneraleDto configGeneraleDto = appConfigService.getConfigGenerale();
        List<NiveauCentre> list = new ArrayList<>();

        if (configGeneraleDto.getTypeCentre() == null) {
            list = niveauCentreJpaRepository.getListVide();
        } else {
            switch (configGeneraleDto.getTypeCentre()) {
                case COMPOSANTE:
                    list = niveauCentreJpaRepository.getListComposante();
                    break;
                case ETAPE:
                    list = niveauCentreJpaRepository.getListEtape();
                    break;
                case MIXTE:
                    list = niveauCentreJpaRepository.getListMixte();
                    break;
            }
        }
        if (centreGestionRepository.etablissementExists()) {
            return list.stream().filter(l -> !l.getLibelle().equalsIgnoreCase("ETABLISSEMENT")).collect(Collectors.toList());
        } else {
            return list.stream().filter(l -> l.getLibelle().equalsIgnoreCase("ETABLISSEMENT")).collect(Collectors.toList());
        }
    }
}
