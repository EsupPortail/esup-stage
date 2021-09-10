package fr.dauphine.estage.controller;

import fr.dauphine.estage.model.Role;
import fr.dauphine.estage.model.RoleEnum;
import fr.dauphine.estage.repository.RoleRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@ApiController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    RoleRepository roleRepository;

    @GetMapping
    @Secure(roles = {RoleEnum.ADM, RoleEnum.ADM_TECH})
    public List<Role> getAll() {
        return roleRepository.findAll();
    }
}
