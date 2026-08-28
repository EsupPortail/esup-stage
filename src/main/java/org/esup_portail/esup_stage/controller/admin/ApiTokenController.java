package org.esup_portail.esup_stage.controller.admin;

import org.esup_portail.esup_stage.controller.ApiController;
import org.esup_portail.esup_stage.dto.ApiTokenFormDto;
import org.esup_portail.esup_stage.dto.ApiTokenSecretDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.model.ApiToken;
import org.esup_portail.esup_stage.repository.ApiTokenRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AdminService;
import org.esup_portail.esup_stage.service.apitoken.ApiTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Gestion des tokens d'accès à l'API publique : un token par application appelante.
 * Réservé aux administrateurs techniques, comme le reste de la configuration.
 */
@ApiController
@RequestMapping("/admin/api-tokens")
public class ApiTokenController {

    @Autowired
    private ApiTokenRepository apiTokenRepository;

    @Autowired
    private ApiTokenService apiTokenService;

    @Autowired
    private AdminService adminService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<ApiToken> search(@RequestParam(name = "page", defaultValue = "1") int page,
                                              @RequestParam(name = "perPage", defaultValue = "50") int perPage,
                                              @RequestParam(name = "predicate", defaultValue = "id") String predicate,
                                              @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder,
                                              @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        adminService.requireAdmin();
        PaginatedResponse<ApiToken> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(apiTokenRepository.count(filters));
        paginatedResponse.setData(apiTokenRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    /**
     * Crée un token. La valeur en clair n'est pas renvoyée ici : elle se récupère à la demande
     * via /{id}/valeur, ce qui évite de la transmettre quand personne ne la consulte.
     */
    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.CREATION})
    public ApiToken create(@RequestBody ApiTokenFormDto form) {
        adminService.requireAdmin();
        return apiTokenService.create(form.getNom(), form.getNomApplication());
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public ApiToken update(@PathVariable("id") Integer id, @RequestBody ApiTokenFormDto form) {
        adminService.requireAdmin();
        return apiTokenService.update(id, form.getNom(), form.getNomApplication());
    }

    @PostMapping("/{id}/renouveler")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public ApiTokenSecretDto renew(@PathVariable("id") Integer id) {
        adminService.requireAdmin();
        ApiToken apiToken = apiTokenService.renew(id);
        return new ApiTokenSecretDto(apiToken, apiTokenService.reveal(id));
    }

    @PutMapping("/{id}/actif")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public ApiToken setActif(@PathVariable("id") Integer id, @RequestParam("actif") boolean actif) {
        adminService.requireAdmin();
        return apiTokenService.setActif(id, actif);
    }

    /** Valeur en clair du token, pour la copie dans le presse-papier. */
    @GetMapping("/{id}/valeur")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ApiTokenSecretDto reveal(@PathVariable("id") Integer id) {
        adminService.requireAdmin();
        return new ApiTokenSecretDto(apiTokenService.getById(id), apiTokenService.reveal(id));
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") Integer id) {
        adminService.requireAdmin();
        apiTokenService.delete(id);
    }
}
