package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Theme;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.ThemeJpaRepository;
import org.esup_portail.esup_stage.repository.ThemeRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/theme")
public class ThemeController {

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ThemeJpaRepository themeJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Theme> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Theme> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(themeRepository.count(filters));
        paginatedResponse.setData(themeRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PostMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.CREATION})
    public Theme create(@RequestBody Theme theme) {
        if (themeRepository.exists(theme.getLibelle(), theme.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Libellé déjà existant");
        }
        theme.setTemEnServ("O");
        theme = themeJpaRepository.saveAndFlush(theme);
        return theme;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public Theme update(@PathVariable("id") int id, @RequestBody Theme requestTheme) {
        Theme theme = themeJpaRepository.findById(id);

        theme.setLibelle(requestTheme.getLibelle());
        if (requestTheme.getTemEnServ() != null) {
            theme.setTemEnServ(requestTheme.getTemEnServ());
        }
        theme = themeJpaRepository.saveAndFlush(theme);
        return theme;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithTheme(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        themeJpaRepository.deleteById(id);
        themeJpaRepository.flush();
    }
}
