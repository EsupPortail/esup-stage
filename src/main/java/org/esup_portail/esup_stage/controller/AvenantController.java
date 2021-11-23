package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.AvenantDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.repository.AvenantRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@ApiController
@RequestMapping("/avenant")
public class AvenantController {

    @Autowired
    AvenantRepository avenantRepository;

    @Autowired
    AvenantJpaRepository avenantJpaRepository;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Avenant> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Avenant> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(avenantRepository.count(filters));
        paginatedResponse.setData(avenantRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.LECTURE})
    public Avenant getById(@PathVariable("id") int id) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
        }
        return avenant;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.CREATION})
    public Avenant create(@Valid @RequestBody AvenantDto avenantDto) {
        Avenant avenant = new Avenant();
        setAvenantData(avenant, avenantDto);
        return avenantJpaRepository.saveAndFlush(avenant);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.MODIFICATION})
    public Avenant update(@PathVariable("id") int id, @Valid @RequestBody AvenantDto avenantDto) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvé");
        }
        setAvenantData(avenant, avenantDto);
        return avenantJpaRepository.saveAndFlush(avenant);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvé");
        }
        avenantJpaRepository.delete(avenant);
        avenantJpaRepository.flush();
        return true;
    }

    private void setAvenantData(Avenant avenant, AvenantDto avenantDto) {
        avenant.setConvention(avenantDto.getConvention());
        avenant.setTitreAvenant(avenantDto.getTitreAvenant());
        avenant.setMotifAvenant(avenantDto.getMotifAvenant());
        avenant.setRupture(avenantDto.getRupture());
        avenant.setModificationPeriode(avenantDto.getModificationPeriode());
        avenant.setDateDebutStage(avenantDto.getDateDebutStage());
        avenant.setDateFinStage(avenantDto.getDateFinStage());
        avenant.setInterruptionStage(avenantDto.getInterruptionStage());
        avenant.setDateDebutInterruption(avenantDto.getDateDebutInterruption());
        avenant.setDateFinInterruption(avenantDto.getDateFinInterruption());
        avenant.setModificationLieu(avenantDto.getModificationLieu());
        avenant.setService(avenantDto.getService());
        avenant.setModificationSujet(avenantDto.getModificationSujet());
        avenant.setSujetStage(avenantDto.getSujetStage());
        avenant.setModificationEnseignant(avenantDto.getModificationEnseignant());
        avenant.setModificationSalarie(avenantDto.getModificationSalarie());
        avenant.setContact(avenantDto.getContact());
        avenant.setValidationAvenant(avenantDto.getValidationAvenant());
        avenant.setEnseignant(avenantDto.getEnseignant());
        avenant.setMontantGratification(avenantDto.getMontantGratification());
        avenant.setUniteGratification(avenantDto.getUniteGratification());
        avenant.setModificationMontantGratification(avenantDto.getModificationMontantGratification());
        avenant.setDateRupture(avenantDto.getDateRupture());
        avenant.setCommentaireRupture(avenantDto.getCommentaireRupture());
        avenant.setMonnaieGratification(avenantDto.getMonnaieGratification());
        avenant.setUniteDuree(avenantDto.getUniteDuree());
    }
}