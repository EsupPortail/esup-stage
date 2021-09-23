package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.ContextDto;
import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.enums.RoleEnum;
import fr.dauphine.estage.exception.NotFoundException;
import fr.dauphine.estage.model.Utilisateur;
import fr.dauphine.estage.repository.UtilisateurJpaRepository;
import fr.dauphine.estage.repository.UtilisateurRepository;
import fr.dauphine.estage.security.ServiceContext;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;


@ApiController
@RequestMapping("/users")
public class UtilisateurController {

    @Autowired
    UtilisateurRepository utilisateurRepository;

    @Autowired
    UtilisateurJpaRepository utilisateurJpaRepository;

    @GetMapping("/connected")
    @Secure()
    public Utilisateur getUserConnected() {
        ContextDto contexteDto = ServiceContext.getServiceContext();
        if (contexteDto != null) {
            return contexteDto.getUtilisateur();
        }
        return null;
    }

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Utilisateur> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Utilisateur> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(utilisateurRepository.count(filters));
        paginatedResponse.setData(utilisateurRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION})
    public Utilisateur update(@PathVariable("id") int id, @RequestBody Utilisateur requestUtilisateur) {
        Utilisateur utilisateur = utilisateurJpaRepository.findByIdActif(id);
        if (utilisateur == null) {
            throw new NotFoundException("Utilisateur non trouvé");
        }
        utilisateur.setNom(requestUtilisateur.getNom());
        utilisateur.setPrenom(requestUtilisateur.getPrenom());
        utilisateur.setRoles(requestUtilisateur.getRoles());
        if (requestUtilisateur.isActif() != null) {
            utilisateur.setActif(requestUtilisateur.isActif());
        }
        utilisateur = utilisateurJpaRepository.saveAndFlush(utilisateur);
        return utilisateur;
    }
}
