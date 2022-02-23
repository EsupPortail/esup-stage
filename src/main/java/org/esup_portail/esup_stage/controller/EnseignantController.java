package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.EnseignantDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Affectation;
import org.esup_portail.esup_stage.model.AffectationId;
import org.esup_portail.esup_stage.model.Enseignant;
import org.esup_portail.esup_stage.repository.EnseignantJpaRepository;
import org.esup_portail.esup_stage.repository.EnseignantRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@ApiController
@RequestMapping("/enseignant")
public class EnseignantController {

    @Autowired
    EnseignantRepository enseignantRepository;

    @Autowired
    EnseignantJpaRepository enseignantJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Enseignant> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Enseignant> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(enseignantRepository.count(filters));
        paginatedResponse.setData(enseignantRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.LECTURE})
    public Enseignant getById(@PathVariable("id") int id) {
        Enseignant enseignant = enseignantJpaRepository.findById(id);
        if (enseignant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Enseignant non trouvée");
        }
        return enseignant;
    }

    @GetMapping("/getByUid/{uid}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.LECTURE})
    public Enseignant getByUid(@PathVariable("uid") String uid) {
        return enseignantJpaRepository.findByUid(uid);
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.CREATION})
    public Enseignant create(@Valid @RequestBody EnseignantDto enseignantDto) {
        Enseignant enseignant = new Enseignant();
        setEnseignantData(enseignant, enseignantDto);
        return enseignantJpaRepository.saveAndFlush(enseignant);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.MODIFICATION})
    public Enseignant update(@PathVariable("id") int id, @Valid @RequestBody EnseignantDto enseignantDto) {
        Enseignant enseignant = enseignantJpaRepository.findById(id);
        if (enseignant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Enseignant non trouvé");
        }
        setEnseignantData(enseignant, enseignantDto);
        return enseignantJpaRepository.saveAndFlush(enseignant);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        Enseignant enseignant = enseignantJpaRepository.findById(id);
        if (enseignant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Enseignant non trouvé");
        }
        enseignantJpaRepository.delete(enseignant);
        enseignantJpaRepository.flush();
        return true;
    }

    private void setEnseignantData(Enseignant enseignant, EnseignantDto enseignantDto) {

        String codeUniversite = appConfigService.getConfigGenerale().getCodeUniversite();

        Affectation affectation = new Affectation();
        AffectationId affectationId = new AffectationId();
        affectationId.setCodeUniversite(codeUniversite);
        affectationId.setCode("");
        affectation.setId(affectationId);
        enseignant.setAffectation(affectation);

        enseignant.setNom(enseignantDto.getNom());
        enseignant.setPrenom(enseignantDto.getPrenom());
        enseignant.setMail(enseignantDto.getMail());
        enseignant.setTel(enseignantDto.getTel());
        enseignant.setTypePersonne(enseignantDto.getTypePersonne());
        enseignant.setUidEnseignant(enseignantDto.getUidEnseignant());
    }
}