package fr.dauphine.estage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.dauphine.estage.dto.ContextDto;
import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.dto.view.Views;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.enums.RoleEnum;
import fr.dauphine.estage.model.Convention;
import fr.dauphine.estage.model.Utilisateur;
import fr.dauphine.estage.model.helper.UtilisateurHelper;
import fr.dauphine.estage.repository.ConventionRepository;
import fr.dauphine.estage.security.ServiceContext;
import fr.dauphine.estage.security.interceptor.Secure;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@ApiController
@RequestMapping("/conventions")
public class ConventionController {

    @Autowired
    ConventionRepository conventionRepository;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonction = AppFonctionEnum.CONVENTION, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Convention> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        ContextDto contexteDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contexteDto.getUtilisateur();
        if (!UtilisateurHelper.isRole(utilisateur, RoleEnum.ADM)) {
            JSONObject jsonFilters = new JSONObject(filters);
            Map<String, Object> currentUser = new HashMap<>();
            currentUser.put("type", "int");
            currentUser.put("value", utilisateur.getId());
            if (UtilisateurHelper.isRole(utilisateur, RoleEnum.RESP_GES) || UtilisateurHelper.isRole(utilisateur, RoleEnum.GES)) {
                Map<String, Object> ges = new HashMap<>();
                ges.put("type", "text");
                ges.put("value", utilisateur.getLogin());
                ges.put("specific", true);
                jsonFilters.put("centreGestion.personnels", ges);
            } else if (UtilisateurHelper.isRole(utilisateur, RoleEnum.ENS)) {
                jsonFilters.append("enseignant", currentUser);
            } else if (UtilisateurHelper.isRole(utilisateur, RoleEnum.ETU)) {
                jsonFilters.append("etudiant", currentUser);
            }

            filters = jsonFilters.toString();
        }

        PaginatedResponse<Convention> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(conventionRepository.count(filters));
        paginatedResponse.setData(conventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }
}
