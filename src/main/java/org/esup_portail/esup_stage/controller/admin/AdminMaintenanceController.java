package org.esup_portail.esup_stage.controller.admin;

import org.esup_portail.esup_stage.controller.ApiController;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Maintenance;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.MaintenanceJpaRepository;
import org.esup_portail.esup_stage.repository.MaintenanceRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.maintenance.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@ApiController
@RequestMapping("/admin/maintenance")
public class AdminMaintenanceController {

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private MaintenanceJpaRepository maintenanceJpaRepository;

    @Autowired
    private MaintenanceService maintenanceService;

    @GetMapping
    @Secure
    public PaginatedResponse<Maintenance> search(@RequestParam(name = "page", defaultValue = "1") int page,
                                                 @RequestParam(name = "perPage", defaultValue = "50") int perPage,
                                                 @RequestParam(name = "predicate", defaultValue = "id") String predicate,
                                                 @RequestParam(name = "sortOrder", defaultValue = "desc") String sortOrder,
                                                 @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        requireAdmin();
        PaginatedResponse<Maintenance> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(maintenanceRepository.count(filters));
        paginatedResponse.setData(maintenanceRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure
    public Maintenance get(@PathVariable("id") Long id) {
        requireAdmin();
        return maintenanceJpaRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Maintenance non trouvee"));
    }

    @PostMapping
    @Secure
    public Maintenance create(@RequestBody Maintenance maintenance) {
        requireAdmin();
        maintenance.setId(null);
        return maintenanceService.save(maintenance);
    }

    @PutMapping("/{id}")
    @Secure
    public Maintenance update(@PathVariable("id") Long id, @RequestBody Maintenance maintenance) {
        requireAdmin();
        if (maintenanceJpaRepository.findById(id).isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND, "Maintenance non trouvee");
        }
        maintenance.setId(id);
        return maintenanceService.save(maintenance);
    }

    @DeleteMapping("/{id}")
    @Secure
    public void delete(@PathVariable("id") Long id) {
        requireAdmin();
        maintenanceService.delete(id);
    }

    @PostMapping("/{id}/activate")
    @Secure
    public Maintenance activate(@PathVariable("id") Long id) {
        requireAdmin();
        return maintenanceService.activate(id);
    }

    @PostMapping("/{id}/deactivate")
    @Secure
    public Maintenance deactivate(@PathVariable("id") Long id) {
        requireAdmin();
        return maintenanceService.deactivate(id);
    }

    private Utilisateur requireAdmin() {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (utilisateur == null || !UtilisateurHelper.isAdmin(utilisateur)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Acces interdit");
        }
        return utilisateur;
    }
}
