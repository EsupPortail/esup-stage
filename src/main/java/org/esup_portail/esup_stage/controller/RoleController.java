package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.AppFonction;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.repository.AppFonctionJpaRepository;
import org.esup_portail.esup_stage.repository.RoleJpaRepository;
import org.esup_portail.esup_stage.repository.RoleRepository;
import org.esup_portail.esup_stage.repository.UtilisateurJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@ApiController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    RoleJpaRepository roleJpaRepository;

    @Autowired
    AppFonctionJpaRepository appFonctionJpaRepository;

    @Autowired
    UtilisateurJpaRepository utilisateurJpaRepository;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Role> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Role> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(roleRepository.count(filters));
        paginatedResponse.setData(roleRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public Role getById(@PathVariable("id") int id) {
        return roleJpaRepository.findById(id);
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.CREATION})
    public Role create(@RequestBody Role role) throws Exception {
        _checkRole(role);
        role = roleJpaRepository.saveAndFlush(role);
        return role;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public Role update(@PathVariable("id") int id, @RequestBody Role roleParam) {
        Role role = roleJpaRepository.findById(id);
        if (role == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Role non trouvé");
        }
        _checkRole(roleParam);
        role = roleJpaRepository.saveAndFlush(roleParam);
        return role;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        Role role = roleJpaRepository.findById(id);
        if (role == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Role non trouvé");
        }
        if (utilisateurJpaRepository.countUserWithRole(role.getId()) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des utilisateurs sont affectés sur ce rôle. La suppression n'est pas possible.");
        }
        roleJpaRepository.delete(role);
        roleJpaRepository.flush();
        return true;
    }

    private void _checkRole(Role role) {
        // vérification code / libelle non existant
        if (roleRepository.exist(role)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Code ou libellé déjà existant");
        }
    }

    @GetMapping("/appFonctions")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public List<AppFonction> getAppFonctions() {
        return appFonctionJpaRepository.findAll();
    }
}
