package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.SendMailTestDto;
import org.esup_portail.esup_stage.dto.TemplateMailGroupeFormDto;
import org.esup_portail.esup_stage.dto.TemplateMailInterface;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.ParamMail;
import org.esup_portail.esup_stage.model.TemplateMailGroupe;
import org.esup_portail.esup_stage.repository.ParamMailJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateMailGroupeJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateMailGroupeRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiController
@RequestMapping("/template-mails-groupe")
public class TemplateMailGroupeController {

    @Autowired
    TemplateMailGroupeRepository templateMailGroupeRepository;

    @Autowired
    TemplateMailGroupeJpaRepository templateMailGroupeJpaRepository;

    @Autowired
    ParamMailJpaRepository paramMailJpaRepository;

    @Autowired
    MailerService mailerService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<TemplateMailGroupe> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        PaginatedResponse<TemplateMailGroupe> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(templateMailGroupeRepository.count(filters));
        paginatedResponse.setData(templateMailGroupeRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        byte[] bytes = templateMailGroupeRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        StringBuilder csv = templateMailGroupeRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public TemplateMailGroupe getById(@PathVariable("id") int id) {
        return templateMailGroupeJpaRepository.findById(id);
    }

    @PostMapping()
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.CREATION})
    public TemplateMailGroupe create(@Valid @RequestBody TemplateMailGroupeFormDto templateMailGroupeFormDto) {
        checkTemplateMailGroupe(templateMailGroupeFormDto);
        TemplateMailGroupe templateMailGroupe = new TemplateMailGroupe();
        templateMailGroupe.setCode(templateMailGroupeFormDto.getCode());
        templateMailGroupe.setLibelle(templateMailGroupeFormDto.getLibelle());
        templateMailGroupe.setObjet(templateMailGroupeFormDto.getObjet());
        templateMailGroupe.setTexte(templateMailGroupeFormDto.getTexte());
        templateMailGroupe = templateMailGroupeJpaRepository.saveAndFlush(templateMailGroupe);
        return templateMailGroupe;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public TemplateMailGroupe update(@PathVariable("id") int id, @Valid @RequestBody TemplateMailGroupeFormDto templateMailGroupeFormDto) {
        checkTemplateMailGroupe(templateMailGroupeFormDto);
        TemplateMailGroupe templateMailGroupe = templateMailGroupeJpaRepository.findById(id);
        if (templateMailGroupe == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Template mail non trouvé");
        }
        templateMailGroupe.setCode(templateMailGroupeFormDto.getCode());
        templateMailGroupe.setLibelle(templateMailGroupeFormDto.getLibelle());
        templateMailGroupe.setObjet(templateMailGroupeFormDto.getObjet());
        templateMailGroupe.setTexte(templateMailGroupeFormDto.getTexte());
        templateMailGroupe = templateMailGroupeJpaRepository.saveAndFlush(templateMailGroupe);
        return templateMailGroupe;
    }

    @GetMapping("/params")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<ParamMail> getParams() {
        return paramMailJpaRepository.findAll();
    }

    private void checkTemplateMailGroupe(TemplateMailInterface templateMailGroupe) {
        // récupération des champs personnalisés existants
        List<ParamMail> champs = paramMailJpaRepository.findAll();

        // vérification des champs personnalisés de l'objet
        List<String> liste = extractChamps(templateMailGroupe.getObjet());
        for (String champ : liste) {
            checkChamp(champs, champ);
        }

        // vérification des champs personnalisés du texte
        liste = extractChamps(templateMailGroupe.getTexte());
        for (String champ : liste) {
            checkChamp(champs, champ);
        }
    }

    private List<String> extractChamps(String texte) {
        List<String> liste = new ArrayList<>();
        Pattern p = Pattern.compile("(.*?)\\$\\{(.*?)\\}.*?");
        Matcher m = p.matcher(texte);
        while (m.find()) {
            liste.add(m.group(2));
        }
        return liste;
    }

    private void checkChamp(List<ParamMail> champs, String champ) {
        boolean isChamps = false;
        for (ParamMail paramMail : champs) {
            if (paramMail.getCode().equals(champ)) {
                isChamps = true;
                break;
            }
        }
        if (!isChamps) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Le champ personnalisé ${" + champ + "} n'existe pas");
        }
    }

    @PostMapping("/send-test")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public boolean testSendMail(@Valid @RequestBody SendMailTestDto sendMailTestDto) {
        mailerService.sendTest(sendMailTestDto, ServiceContext.getUtilisateur());
        return true;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        templateMailGroupeJpaRepository.deleteById(id);
        templateMailGroupeJpaRepository.flush();
    }
}
