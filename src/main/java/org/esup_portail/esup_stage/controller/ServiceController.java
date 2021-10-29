package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Service;
import org.esup_portail.esup_stage.repository.ServiceJpaRepository;
import org.esup_portail.esup_stage.repository.ServiceRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@ApiController
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    ServiceJpaRepository serviceJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Service> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Service> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(serviceRepository.count(filters));
        paginatedResponse.setData(serviceRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.LECTURE})
    public Service getById(@PathVariable("id") int id) {
        Service service = serviceJpaRepository.findById(id);
        if (service == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouvée");
        }
        return service;
    }

    @GetMapping("/getByStructure/{id}")
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.LECTURE})
    public List<Service> getByStructure(@PathVariable("id") int id) {
        List<Service> service = serviceJpaRepository.findByStructure(id);
        if (service == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouvée");
        }
        return service;
    }

    @PostMapping
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.CREATION})
    public Service create(@Valid @RequestBody Service _service) {
        return serviceJpaRepository.saveAndFlush(_service);
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.MODIFICATION})
    public Service update(@PathVariable("id") int id, @Valid @RequestBody Service _service) {
        Service service = serviceJpaRepository.findById(id);
        if (service == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouvée");
        }
        service.setNom(_service.getNom());
        service.setVoie(_service.getVoie());
        service.setCodePostal(_service.getCodePostal());
        service.setBatimentResidence(_service.getBatimentResidence());
        service.setCommune(_service.getCommune());
        service.setPays(_service.getPays());
        service.setTelephone(_service.getTelephone());
        service = serviceJpaRepository.saveAndFlush(service);
        return service;
    }

}
