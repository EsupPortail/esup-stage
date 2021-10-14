package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.ContratOffre;
import fr.dauphine.estage.repository.ContratOffreJpaRepository;
import fr.dauphine.estage.repository.ContratOffreRepository;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/contrat-offre")
public class ContratOffreController {

    @Autowired
    ContratOffreRepository contratOffreRepository;

    @Autowired
    ContratOffreJpaRepository contratOffreJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<ContratOffre> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<ContratOffre> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(contratOffreRepository.count(filters));
        paginatedResponse.setData(contratOffreRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PostMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.CREATION})
    public ContratOffre create(@RequestBody ContratOffre contratOffre) {
        if (contratOffreRepository.exists(contratOffre.getLibelle(), contratOffre.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Libellé déjà existant");
        }
        contratOffre.setTemEnServ("O");
        contratOffre.setModifiable(true);
        contratOffre = contratOffreJpaRepository.saveAndFlush(contratOffre);
        return contratOffre;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public ContratOffre update(@PathVariable("id") int id, @RequestBody ContratOffre requestContratOffre) {
        ContratOffre contratOffre = contratOffreJpaRepository.findById(id);

        contratOffre.setLibelle(requestContratOffre.getLibelle());
        if (requestContratOffre.getTemEnServ() != null) {
            contratOffre.setTemEnServ(requestContratOffre.getTemEnServ());
        }
        contratOffre = contratOffreJpaRepository.saveAndFlush(contratOffre);
        return contratOffre;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithContratOffre(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        contratOffreJpaRepository.deleteById(id);
        contratOffreJpaRepository.flush();
    }
}
