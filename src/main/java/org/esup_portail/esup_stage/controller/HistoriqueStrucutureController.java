package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.HistoriqueStructure;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.repository.HistoriqueStructureJpaRepository;
import org.esup_portail.esup_stage.repository.StructureJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;

@ApiController
@RequestMapping("/historique_structures")
public class HistoriqueStrucutureController {

    @Autowired
    private HistoriqueStructureJpaRepository historiqueStructureJpaRepository;

    @Autowired
    private StructureJpaRepository structureJpaRepository;

    @GetMapping("/structures/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC, AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION})
    public List<HistoriqueStructure> getHistorique(@PathVariable("id") int id) {
        Structure structure = structureJpaRepository.findById(id);
        if(structure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Structure non trouvée");
        }
        List<HistoriqueStructure> historiqueStructures = historiqueStructureJpaRepository.findByStructure(structure);
        historiqueStructures.sort(Comparator.comparing(HistoriqueStructure::getOperationDate).reversed());
        return historiqueStructures;

    }
}
