package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Effectif;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.ContenuJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.EffectifJpaRepository;
import org.esup_portail.esup_stage.repository.EffectifRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@ApiController
@RequestMapping("/effectifs")
public class EffectifController {

    @Autowired
    EffectifRepository effectifRepository;

    @Autowired
    EffectifJpaRepository effectifJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    ContenuJpaRepository contenuJpaRepository;

    @GetMapping
    @Secure
    public List<Effectif> getAll(@RequestParam(value = "actif", required = false) boolean onlyActif) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (onlyActif || UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            return effectifJpaRepository.findAllActif();
        }
        return effectifJpaRepository.findAll();
    }

    @GetMapping("/search")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Effectif> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Effectif> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(effectifRepository.count(filters));
        paginatedResponse.setData(effectifRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        byte[] bytes = effectifRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        StringBuilder csv = effectifRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public Effectif create(@RequestBody Effectif effectif) {
        if (effectifRepository.exists(effectif.getLibelle(), effectif.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, contenuJpaRepository.findByCode("NOMENCLATURE_LIBELLE_EXISTANT").getTexte());
        }
        effectif.setTemEnServ("O");
        effectif.setModifiable(true);
        effectif = effectifJpaRepository.saveAndFlush(effectif);
        return effectif;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public Effectif update(@PathVariable("id") int id, @RequestBody Effectif requestEffectif) {
        Effectif niveauFormation = effectifJpaRepository.findById(id);

        niveauFormation.setLibelle(requestEffectif.getLibelle());
        if (requestEffectif.getTemEnServ() != null) {
            niveauFormation.setTemEnServ(requestEffectif.getTemEnServ());
        }
        niveauFormation = effectifJpaRepository.saveAndFlush(niveauFormation);
        return niveauFormation;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithEffectif(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        effectifJpaRepository.deleteById(id);
        effectifJpaRepository.flush();
    }
}
