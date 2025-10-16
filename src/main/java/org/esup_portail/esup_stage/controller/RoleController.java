package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.AppFonction;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.RoleAppFonction;
import org.esup_portail.esup_stage.repository.AppFonctionJpaRepository;
import org.esup_portail.esup_stage.repository.RoleJpaRepository;
import org.esup_portail.esup_stage.repository.RoleRepository;
import org.esup_portail.esup_stage.repository.UtilisateurJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        byte[] bytes = roleRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        StringBuilder csv = roleRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
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

    @GetMapping("/droits")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public RoleAppFonction getRoleAppFonction(@RequestParam("role") String codeRole,
                                              @RequestParam("appFonction") String codeAppFonction) {

        Role role = roleJpaRepository.findOneByCode(codeRole);
        if (role == null) {
            System.out.println("role");
            throw new AppException(HttpStatus.NOT_FOUND, "Role non trouvé");
        }

        final AppFonctionEnum appFonctionEnum;
        try {
            appFonctionEnum = AppFonctionEnum.valueOf(codeAppFonction);
        } catch (IllegalArgumentException e) {
            System.out.println("appfonction");
            throw new AppException(HttpStatus.BAD_REQUEST, "Code appFonction invalide : " + codeAppFonction);
        }

        return Optional.ofNullable(role.getRoleAppFonctions())
                .orElse(Collections.emptyList())
                .stream()
                .filter(r -> r.getAppFonction() != null && r.getAppFonction().getCode() == appFonctionEnum)
                .findFirst()
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Droit non trouvé pour ce rôle et cette fonction"));
    }

}
