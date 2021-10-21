package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.model.NafN5;
import org.esup_portail.esup_stage.repository.NafN5JpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@ApiController
@RequestMapping("/nafn5")
public class NafN5Controller {

    @Autowired
    NafN5JpaRepository nafN5JpaRepository;

    @GetMapping("/{code}")
    @Secure
    public NafN5 getByCode(@PathVariable("code") String code) {
        return nafN5JpaRepository.findByCode(code);
    }
}
