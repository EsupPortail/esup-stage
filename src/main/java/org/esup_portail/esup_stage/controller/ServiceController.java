package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.ServiceDto;
import org.esup_portail.esup_stage.dto.ServiceFormDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Pays;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Service;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.ContactJpaRepository;
import org.esup_portail.esup_stage.repository.PaysJpaRepository;
import org.esup_portail.esup_stage.repository.ServiceJpaRepository;
import org.esup_portail.esup_stage.repository.ServiceRepository;
import org.esup_portail.esup_stage.repository.StructureJpaRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.security.permission.ServicePermissionEvaluator;
import org.esup_portail.esup_stage.service.ConfidentialiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@ApiController
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    ServiceJpaRepository serviceJpaRepository;

    @Autowired
    ContactJpaRepository contactJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    StructureJpaRepository structureJpaRepository;

    @Autowired
    PaysJpaRepository paysJpaRepository;

    @Autowired
    ConfidentialiteService confidentialiteService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<ServiceDto> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        PaginatedResponse<ServiceDto> paginatedResponse = new PaginatedResponse<>();
        List<Service> services;

        if (isGestionnaire(utilisateur)) {
            List<CentreGestion> centresDemandeur = getCurrentGestionnaireCentres(utilisateur);
            if (centresDemandeur.isEmpty()) {
                throw new AppException(HttpStatus.FORBIDDEN, "Impossible de determiner le centre de gestion du gestionnaire");
            }

            List<Integer> centreIds = centresDemandeur.stream().map(CentreGestion::getId).toList();
            paginatedResponse.setTotal(serviceRepository.countVisibleForCentres(centreIds, filters));
            services = serviceRepository.findPaginatedVisibleForCentres(centreIds, page, perPage, predicate, sortOrder, filters);
        } else {
            paginatedResponse.setTotal(serviceRepository.count(filters));
            services = serviceRepository.findPaginated(page, perPage, predicate, sortOrder, filters);
        }

        paginatedResponse.setData(services.stream().map(this::buildServiceDto).toList());
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.LECTURE})
    public ServiceDto getById(@PathVariable("id") int id) {
        Service service = serviceJpaRepository.findById(id);
        if (service == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouve");
        }

        assertCanViewService(service, ServiceContext.getUtilisateur());
        return buildServiceDto(service);
    }

    @GetMapping("/getByStructure/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC, AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<ServiceDto> getByStructure(@PathVariable("id") int id, @RequestParam(value = "idCentreGestion", required = false, defaultValue = "-1") Integer idCentreGestion) {
        List<Service> services = serviceJpaRepository.findByStructure(id);
        if (services == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouve");
        }

        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (!isGestionnaire(utilisateur)) {
            return services.stream().map(this::buildServiceDto).toList();
        }

        List<CentreGestion> centresDemandeur = getCurrentGestionnaireCentres(utilisateur);
        if (centresDemandeur.isEmpty()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Impossible de determiner le centre de gestion du gestionnaire");
        }

        List<Service> filteredServices = services.stream()
                .filter(service -> canViewService(centresDemandeur, service))
                .toList();

        if (idCentreGestion != null && idCentreGestion != -1) {
            CentreGestion centreGestionConvention = centreGestionJpaRepository.findById(idCentreGestion.intValue());
            if (centreGestionConvention == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouve");
            }
            filteredServices = filteredServices.stream()
                    .filter(service -> canUseServiceForConvention(service, centreGestionConvention, centresDemandeur))
                    .toList();
        }

        return filteredServices.stream().map(this::buildServiceDto).toList();
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.CREATION})
    public Service create(@Valid @RequestBody ServiceFormDto serviceFormDto) {
        Service service = new Service();
        setServiceData(service, serviceFormDto);

        Structure structure = structureJpaRepository.findById(serviceFormDto.getIdStructure());
        if (structure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouve");
        }

        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        CentreGestion centreGestion;
        if (isGestionnaire(utilisateur)) {
            if (serviceFormDto.getIdCentreGestion() != null) {
                centreGestion = centreGestionJpaRepository.findById(serviceFormDto.getIdCentreGestion().intValue());
            } else if (structure.getCentreGestionProprietaire() != null) {
                centreGestion = structure.getCentreGestionProprietaire();
            } else {
                centreGestion = centreGestionJpaRepository.getCentreEtablissement();
            }
        } else {
            centreGestion = structure.getCentreGestionProprietaire() != null
                    ? structure.getCentreGestionProprietaire()
                    : centreGestionJpaRepository.getCentreEtablissement();
        }
        if (centreGestion == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouve");
        }

        service.setCentreGestion(centreGestion);
        service.setStructure(structure);

        return serviceJpaRepository.saveAndFlush(service);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.MODIFICATION}, evaluator = ServicePermissionEvaluator.class)
    public Service update(@PathVariable("id") int id, @Valid @RequestBody ServiceFormDto serviceFormDto) {
        Service service = serviceJpaRepository.findById(id);
        setServiceData(service, serviceFormDto);
        return serviceJpaRepository.saveAndFlush(service);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        Service service = serviceJpaRepository.findById(id);
        if (service == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouve");
        }
        if (contactJpaRepository.countContactWithService(service.getId()) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des contacts existent sur ce service. La suppression n'est pas possible.");
        }
        serviceJpaRepository.delete(service);
        serviceJpaRepository.flush();
        return true;
    }

    private void setServiceData(Service service, ServiceFormDto serviceFormDto) {
        if (service == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouve");
        }
        Pays pays = paysJpaRepository.findById(serviceFormDto.getIdPays());
        if (pays == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Pays non trouve");
        }
        service.setNom(serviceFormDto.getNom());
        service.setVoie(serviceFormDto.getVoie());
        service.setCodePostal(serviceFormDto.getCodePostal());
        service.setBatimentResidence(serviceFormDto.getBatimentResidence());
        service.setCommune(serviceFormDto.getCommune());
        service.setPays(pays);
        service.setTelephone(serviceFormDto.getTelephone());
    }

    private boolean isGestionnaire(Utilisateur utilisateur) {
        return UtilisateurHelper.isRole(utilisateur, Role.GES)
                || UtilisateurHelper.isRole(utilisateur, Role.RESP_GES);
    }

    private List<CentreGestion> getCurrentGestionnaireCentres(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getUid() == null || utilisateur.getUid().isBlank()) {
            return new ArrayList<>();
        }
        return centreGestionJpaRepository.findAllByGestionnaireUid(utilisateur.getUid());
    }

    private boolean canViewService(List<CentreGestion> centresDemandeur, Service service) {
        for (CentreGestion centreDemandeur : centresDemandeur) {
            if (confidentialiteService.canViewService(centreDemandeur, service)) {
                return true;
            }
        }
        return false;
    }

    private boolean canUseServiceForConvention(Service service, CentreGestion centreGestionConvention, List<CentreGestion> centresDemandeur) {
        CentreGestion centreGestionService = service != null ? service.getCentreGestion() : null;
        return centreGestionService != null
                && (centreGestionService.getId() == centreGestionConvention.getId()
                || isAttachedToCentre(centresDemandeur, centreGestionService)
                || confidentialiteService.isNoConfidentiality(centreGestionService)
                || confidentialiteService.isCentreEtablissement(centreGestionService));
    }

    private boolean isAttachedToCentre(List<CentreGestion> centresDemandeur, CentreGestion centreGestion) {
        return centresDemandeur != null
                && centreGestion != null
                && centresDemandeur.stream().anyMatch(centreDemandeur -> centreDemandeur.getId() == centreGestion.getId());
    }

    private void assertCanViewService(Service service, Utilisateur utilisateur) {
        if (!isGestionnaire(utilisateur)) {
            return;
        }
        List<CentreGestion> centresDemandeur = getCurrentGestionnaireCentres(utilisateur);
        if (centresDemandeur.isEmpty() || !canViewService(centresDemandeur, service)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Vous n'avez pas acces a ce service");
        }
    }

    private ServiceDto buildServiceDto(Service service) {
        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId(service.getId());
        serviceDto.setNom(service.getNom());
        serviceDto.setVoie(service.getVoie());
        serviceDto.setCodePostal(service.getCodePostal());
        serviceDto.setBatimentResidence(service.getBatimentResidence());
        serviceDto.setCommune(service.getCommune());
        serviceDto.setTelephone(service.getTelephone());
        serviceDto.setPays(service.getPays());
        serviceDto.setIdCentreGestion(service.getCentreGestion().getId());
        return serviceDto;
    }
}
