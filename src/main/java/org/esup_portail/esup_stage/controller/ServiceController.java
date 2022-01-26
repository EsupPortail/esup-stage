package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ContextDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.ServiceFormDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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


    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Service> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Service> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(serviceRepository.count(filters));
        paginatedResponse.setData(serviceRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.LECTURE})
    public Service getById(@PathVariable("id") int id) {
        Service service = serviceJpaRepository.findById(id);
        if (service == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouvée");
        }
        return service;
    }

    @GetMapping("/getByStructure/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.LECTURE})
    public List<Service> getByStructure(@PathVariable("id") int id, @RequestParam(value = "idCentreGestion", required = false, defaultValue = "-1") Integer idCentreGestion) {
        List<Service> services = serviceJpaRepository.findByStructure(id);
        if (services == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouvée");
        }

        ContextDto contexteDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contexteDto.getUtilisateur();
        /*
        * Si idCentreGestion != -1, on est dans le cadre d'une convention
        *
        * Dans ce cas pour les utilisateurs gestionnaires, on ne renvoit que les services qui sont rattachés au centre de la convention ou
          qui ont un centre de gestion avec code confidentialité égale à 0 ou au centre de gestion de type établissement
        * */
        if ((UtilisateurHelper.isRole(utilisateur, Role.GES) || UtilisateurHelper.isRole(utilisateur, Role.RESP_GES)) && idCentreGestion != -1) {
            CentreGestion centreGestion = centreGestionJpaRepository.findById(idCentreGestion.intValue());
            if (centreGestion == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouvé");
            }
            List<Service> filteredservices = new ArrayList<Service>();
            for(Service service : services){
                if (service.getCentreGestion().getCodeConfidentialite().getCode().equals("0") || service.getCentreGestion().getId() == centreGestion.getId() ||
                        service.getCentreGestion().getNiveauCentre().getLibelle().equals("ETABLISSEMENT")){
                    filteredservices.add(service);
                }
            }
            return filteredservices;
        }


        return services;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.CREATION})
    public Service create(@Valid @RequestBody ServiceFormDto serviceFormDto) {
        Service service = new Service();
        setServiceData(service, serviceFormDto);

        Structure structure = structureJpaRepository.findById(serviceFormDto.getIdStructure());
        if (structure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouvée");
        }

        ContextDto contexteDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contexteDto.getUtilisateur();
        CentreGestion centreGestion;
        //Ajoute le centreGestion de l'utilisateur qui a créé le contact pour les utilisateurs gestionnaires.
        if (UtilisateurHelper.isRole(utilisateur, Role.GES) || UtilisateurHelper.isRole(utilisateur, Role.RESP_GES)) {
            if (serviceFormDto.getIdCentreGestion() != null) {
                centreGestion = centreGestionJpaRepository.findById(serviceFormDto.getIdCentreGestion().intValue());
            } else {
                centreGestion = centreGestionJpaRepository.getCentreEtablissement();
            }
        } else {
            centreGestion = centreGestionJpaRepository.getCentreEtablissement();
        }
        if (centreGestion == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouvé");
        }

        service.setCentreGestion(centreGestion);
        service.setStructure(structure);

        return serviceJpaRepository.saveAndFlush(service);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.MODIFICATION})
    public Service update(@PathVariable("id") int id, @Valid @RequestBody ServiceFormDto serviceFormDto) {
        Service service = serviceJpaRepository.findById(id);
        setServiceData(service, serviceFormDto);
        service = serviceJpaRepository.saveAndFlush(service);
        return service;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        Service service = serviceJpaRepository.findById(id);
        if (service == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouvé");
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
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouvé");
        }
        Pays pays = paysJpaRepository.findById(serviceFormDto.getIdPays());
        if (pays == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Pays non trouvé");
        }
        service.setNom(serviceFormDto.getNom());
        service.setVoie(serviceFormDto.getVoie());
        service.setCodePostal(serviceFormDto.getCodePostal());
        service.setBatimentResidence(serviceFormDto.getBatimentResidence());
        service.setCommune(serviceFormDto.getCommune());
        service.setPays(pays);
        service.setTelephone(serviceFormDto.getTelephone());

    }

}
