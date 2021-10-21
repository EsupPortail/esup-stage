package fr.dauphine.estage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.dto.view.Views;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.Structure;
import fr.dauphine.estage.repository.StructureJpaRepository;
import fr.dauphine.estage.repository.StructureRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@ApiController
@RequestMapping("/structures")
public class StructureController {

    @Autowired
    StructureRepository structureRepository;

    @Autowired
    StructureJpaRepository structureJpaRepository;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Structure> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Structure> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(structureRepository.count(filters));
        paginatedResponse.setData(structureRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.LECTURE})
    public Structure getById(@PathVariable("id") int id) {
        Structure structure = structureJpaRepository.findById(id);
        if (structure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Structure non trouvée");
        }
        return structure;
    }

    @PostMapping
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.CREATION})
    public Structure create(@Valid @RequestBody Structure _structure) {
        check(_structure);
        return structureJpaRepository.saveAndFlush(_structure);
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.MODIFICATION})
    public Structure update(@PathVariable("id") int id, @Valid @RequestBody Structure _structure) {
        Structure structure = structureJpaRepository.findById(id);
        if (structure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Structure non trouvée");
        }
        check(_structure);
        structure.setRaisonSociale(_structure.getRaisonSociale());
        structure.setNumeroSiret(_structure.getNumeroSiret());
        structure.setEffectif(_structure.getEffectif());
        structure.setTypeStructure(_structure.getTypeStructure());
        structure.setStatutJuridique(_structure.getStatutJuridique());
        structure.setNafN5(_structure.getNafN5());
        structure.setActivitePrincipale(_structure.getActivitePrincipale());
        structure.setVoie(_structure.getVoie());
        structure.setCodePostal(_structure.getCodePostal());
        structure.setBatimentResidence(_structure.getBatimentResidence());
        structure.setCommune(_structure.getCommune());
        structure.setLibCedex(_structure.getLibCedex());
        structure.setPays(_structure.getPays());
        structure.setMail(_structure.getMail());
        structure.setTelephone(_structure.getTelephone());
        structure.setSiteWeb(_structure.getSiteWeb());
        structure.setFax(_structure.getFax());
        structure = structureJpaRepository.saveAndFlush(structure);
        return structure;
    }

    private void check(Structure structure) {
        if (structure.getNafN5() == null && structure.getActivitePrincipale() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "L'un des 2 champs \"code APE\" ou \"activité principale\" doit être renseigné");
        }
    }
}
