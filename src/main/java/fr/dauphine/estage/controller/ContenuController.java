package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.Contenu;
import fr.dauphine.estage.repository.ContenuJpaRepository;
import fr.dauphine.estage.repository.ContenuRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiController
@RequestMapping("/contenus")
public class ContenuController {

    @Autowired
    ContenuRepository contenuRepository;

    @Autowired
    ContenuJpaRepository contenuJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.PARAM_GLOBAL, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Contenu> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        PaginatedResponse<Contenu> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(contenuRepository.count(filters));
        paginatedResponse.setData(contenuRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/libelle")
    @Secure
    public List<Contenu> getLibelle() {
        return contenuJpaRepository.getLibelle();
    }

    @GetMapping("/{code}")
    @Secure
    public Contenu get(@PathVariable("code") String code) {
        return contenuJpaRepository.findByCode(code);
    }

    @PutMapping("/{code}")
    @Secure(fonction = AppFonctionEnum.PARAM_GLOBAL, droits = {DroitEnum.MODIFICATION})
    public Contenu update(@PathVariable("code") String code, @RequestBody Contenu contenuRequest) {
        Contenu contenu = contenuJpaRepository.findByCode(code);
        if (contenu == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contenu " + code + " non trouvé");
        }
        contenu.setLibelle(contenuRequest.isLibelle());
        contenu.setTexte(contenuRequest.getTexte());
        contenu = contenuJpaRepository.saveAndFlush(contenu);
        return contenu;
    }
}
