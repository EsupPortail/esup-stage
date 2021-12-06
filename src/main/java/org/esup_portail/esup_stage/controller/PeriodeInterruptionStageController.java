package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.PeriodeInterruptionStageDto;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.PeriodeInterruptionStage;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeInterruptionStageJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeInterruptionStageRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@ApiController
@RequestMapping("/periode-interruption-stage")
public class PeriodeInterruptionStageController {

    @Autowired
    PeriodeInterruptionStageRepository periodeInterruptionStageRepository;

    @Autowired
    PeriodeInterruptionStageJpaRepository periodeInterruptionStageJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<PeriodeInterruptionStage> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<PeriodeInterruptionStage> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(periodeInterruptionStageRepository.count(filters));
        paginatedResponse.setData(periodeInterruptionStageRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @JsonView(Views.List.class)
    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public PeriodeInterruptionStage getById(@PathVariable("id") int id) {
        PeriodeInterruptionStage periodeInterruptionStage = periodeInterruptionStageJpaRepository.findById(id);
        if (periodeInterruptionStage == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "PeriodeInterruptionStage non trouvée");
        }
        return periodeInterruptionStage;
    }

    @JsonView(Views.List.class)
    @GetMapping("/getByConvention/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<PeriodeInterruptionStage> getByConvention(@PathVariable("id") int id) {
        return periodeInterruptionStageJpaRepository.findByConvention(id);
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.CREATION})
    public PeriodeInterruptionStage create(@Valid @RequestBody PeriodeInterruptionStageDto periodeInterruptionStageDto) {
        PeriodeInterruptionStage periodeInterruptionStage = new PeriodeInterruptionStage();
        setPeriodeInterruptionStageData(periodeInterruptionStage, periodeInterruptionStageDto);
        return periodeInterruptionStageJpaRepository.saveAndFlush(periodeInterruptionStage);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public PeriodeInterruptionStage update(@PathVariable("id") int id, @Valid @RequestBody PeriodeInterruptionStageDto periodeInterruptionStageDto) {
        PeriodeInterruptionStage periodeInterruptionStage = periodeInterruptionStageJpaRepository.findById(id);
        if (periodeInterruptionStage == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "PeriodeInterruptionStage non trouvée");
        }
        setPeriodeInterruptionStageData(periodeInterruptionStage, periodeInterruptionStageDto);
        return periodeInterruptionStageJpaRepository.saveAndFlush(periodeInterruptionStage);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        PeriodeInterruptionStage periodeInterruptionStage = periodeInterruptionStageJpaRepository.findById(id);
        if (periodeInterruptionStage == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "PeriodeInterruptionStage non trouvé");
        }
        periodeInterruptionStageJpaRepository.delete(periodeInterruptionStage);
        periodeInterruptionStageJpaRepository.flush();
        return true;
    }

    private void setPeriodeInterruptionStageData(PeriodeInterruptionStage periodeInterruptionStage, PeriodeInterruptionStageDto periodeInterruptionStageDto) {

        Convention convention = conventionJpaRepository.findById(periodeInterruptionStageDto.getIdConvention());
        if (periodeInterruptionStage == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        periodeInterruptionStage.setDateDebutInterruption(periodeInterruptionStageDto.getDateDebutInterruption());
        periodeInterruptionStage.setDateFinInterruption(periodeInterruptionStageDto.getDateFinInterruption());
        periodeInterruptionStage.setConvention(convention);
    }
}