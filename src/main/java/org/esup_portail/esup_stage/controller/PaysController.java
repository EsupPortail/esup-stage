package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.PaysDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Pays;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.PaysJpaRepository;
import org.esup_portail.esup_stage.repository.PaysRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@ApiController
@RequestMapping("/pays")
public class PaysController {

    @Autowired
    PaysRepository paysRepository;

    @Autowired
    PaysJpaRepository paysJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure()
    public PaginatedResponse<PaysDto> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Pays> paginatedResponsePays = new PaginatedResponse<>();
        PaginatedResponse<PaysDto> paginatedResponsePaysDto = new PaginatedResponse<>();
        paginatedResponsePaysDto.setTotal(paysRepository.count(filters));
        paginatedResponsePays.setData(paysRepository.findPaginated(page, perPage, predicate, sortOrder, filters));

        List<PaysDto> paysDtoList = new ArrayList<>();
        for (Pays pays : paginatedResponsePays.getData()) {
            PaysDto dto = new PaysDto(pays.getId(), pays.getLib(), pays.getTemEnServPays());
            paysDtoList.add(dto);
        }

        paginatedResponsePaysDto.setData(paysDtoList);
        return paginatedResponsePaysDto;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public Pays create(@RequestBody Pays pays) {
        if (paysRepository.exists(pays)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Libellé déjà existant");
        }
        pays.setTemEnServPays("O");
        pays = paysJpaRepository.saveAndFlush(pays);
        return pays;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public PaysDto update(@PathVariable("id") int id, @RequestBody PaysDto requestPays) {
        Pays pays = paysJpaRepository.findById(id);

        pays.setLib(requestPays.getLibelle());
        if (requestPays.getTemEnServ() != null) {
            pays.setTemEnServPays(requestPays.getTemEnServ());
        }
        pays = paysJpaRepository.saveAndFlush(pays);
        PaysDto returnPays = new PaysDto(pays.getId(), pays.getLib(), pays.getTemEnServPays());
        return returnPays;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithPays(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        paysJpaRepository.deleteById(id);
        paysJpaRepository.flush();
    }
}
