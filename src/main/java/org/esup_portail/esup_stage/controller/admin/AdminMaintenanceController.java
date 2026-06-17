package org.esup_portail.esup_stage.controller.admin;

import org.esup_portail.esup_stage.controller.ApiController;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Maintenance;
import org.esup_portail.esup_stage.repository.MaintenanceJpaRepository;
import org.esup_portail.esup_stage.repository.MaintenanceRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AdminService;
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
    
    @Autowired
    private AdminService adminService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Maintenance> search(@RequestParam(name = "page", defaultValue = "1") int page,
                                                 @RequestParam(name = "perPage", defaultValue = "50") int perPage,
                                                 @RequestParam(name = "predicate", defaultValue = "id") String predicate,
                                                 @RequestParam(name = "sortOrder", defaultValue = "desc") String sortOrder,
                                                 @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        adminService.requireAdmin();
        PaginatedResponse<Maintenance> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(maintenanceRepository.count(filters));
        paginatedResponse.setData(maintenanceRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public Maintenance get(@PathVariable("id") Long id) {
        adminService.requireAdmin();
        return maintenanceJpaRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Maintenance non trouvee"));
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public Maintenance create(@RequestBody Maintenance maintenance) {
        adminService.requireAdmin();
        maintenance.setId(null);
        return maintenanceService.save(maintenance);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public Maintenance update(@PathVariable("id") Long id, @RequestBody Maintenance maintenance) {
        adminService.requireAdmin();
        if (maintenanceJpaRepository.findById(id).isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND, "Maintenance non trouvee");
        }
        maintenance.setId(id);
        return maintenanceService.save(maintenance);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public void delete(@PathVariable("id") Long id) {
        adminService.requireAdmin();
        maintenanceService.delete(id);
    }

    @PostMapping("/{id}/activate")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public Maintenance activate(@PathVariable("id") Long id) {
        adminService.requireAdmin();
        return maintenanceService.activate(id);
    }

    @PostMapping("/{id}/deactivate")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public Maintenance deactivate(@PathVariable("id") Long id) {
        adminService.requireAdmin();
        return maintenanceService.deactivate(id);
    }
    
}
