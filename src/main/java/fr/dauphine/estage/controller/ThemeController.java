package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.Theme;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.ThemeJpaRepository;
import fr.dauphine.estage.repository.ThemeRepository;
import fr.dauphine.estage.security.interceptor.Secure;
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
