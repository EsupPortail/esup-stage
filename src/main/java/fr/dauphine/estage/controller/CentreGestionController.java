package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.model.CentreGestion;
import fr.dauphine.estage.model.PersonnelCentreGestion;
import fr.dauphine.estage.model.Utilisateur;
import fr.dauphine.estage.repository.CentreGestionJpaRepository;
import fr.dauphine.estage.repository.CentreGestionRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@ApiController
@RequestMapping("/centre-gestion")
public class CentreGestionController {

    @Autowired
    CentreGestionRepository centreGestionRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    UtilisateurController utilisateurController;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.PARAM_CENTRE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<CentreGestion> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<CentreGestion> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(centreGestionRepository.count(filters));
        paginatedResponse.setData(centreGestionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));

        if (predicate.equals("personnels")) {
            Utilisateur currentUser = utilisateurController.getUserConnected();
            List<CentreGestion> list =  paginatedResponse.getData();
            Predicate<PersonnelCentreGestion> condition = value -> value.getUidPersonnel().equals(currentUser.getLogin());
            list.sort((a, b) -> Boolean.compare(a.getPersonnels().stream().anyMatch(condition), b.getPersonnels().stream().anyMatch(condition)));

            if (sortOrder.equals("asc"))
                Collections.reverse(list);
        }

        return paginatedResponse;
    }
}
