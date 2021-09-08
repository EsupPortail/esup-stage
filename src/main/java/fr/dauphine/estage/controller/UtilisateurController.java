package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.ContextDto;
import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.model.RoleEnum;
import fr.dauphine.estage.model.Utilisateur;
import fr.dauphine.estage.repository.UtilisateurRepository;
import fr.dauphine.estage.security.ServiceContext;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


@ApiController
@RequestMapping("/users")
public class UtilisateurController {

    @Autowired
    UtilisateurRepository utilisateurRepository;

    @GetMapping("/connected")
    @Secure(roles = {})
    public Utilisateur getUserConnected() {
        ContextDto contexteDto = ServiceContext.getServiceContext();
        if (contexteDto != null) {
            return contexteDto.getUtilisateur();
        }
        return null;
    }

    @GetMapping
    @Secure(roles = {RoleEnum.ADM_TECH, RoleEnum.ADM})
    public PaginatedResponse search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse paginatedResponse = new PaginatedResponse();
        paginatedResponse.setTotal(utilisateurRepository.count(filters));
        paginatedResponse.setData(utilisateurRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }
}
