package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ContextDto;
import org.esup_portail.esup_stage.dto.GroupeEtudiantDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.GroupeEtudiant;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.GroupeEtudiantJpaRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@ApiController
@RequestMapping("/groupeEtudiant")
public class GroupeEtudiantController {

    @Autowired
    GroupeEtudiantJpaRepository groupeEtudiantJpaRepository;

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public GroupeEtudiant getById(@PathVariable("id") int id) {
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvée");
        }
        return groupeEtudiant;
    }


    @GetMapping("/brouillon")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public GroupeEtudiant getBrouillon() {
        ContextDto contexteDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contexteDto.getUtilisateur();
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findBrouillon(utilisateur.getLogin());
        if (groupeEtudiant == null) {
            groupeEtudiant = new GroupeEtudiant();
        }
        return groupeEtudiant;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.CREATION})
    public GroupeEtudiant create(@Valid @RequestBody GroupeEtudiantDto groupeEtudiantDto) {
        GroupeEtudiant groupeEtudiant = new GroupeEtudiant();
        setGroupeEtudiantData(groupeEtudiant, groupeEtudiantDto);
        return groupeEtudiantJpaRepository.saveAndFlush(groupeEtudiant);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public GroupeEtudiant update(@PathVariable("id") int id, @Valid @RequestBody GroupeEtudiantDto groupeEtudiantDto) {
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvé");
        }
        setGroupeEtudiantData(groupeEtudiant, groupeEtudiantDto);
        return groupeEtudiantJpaRepository.saveAndFlush(groupeEtudiant);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvé");
        }
        groupeEtudiantJpaRepository.delete(groupeEtudiant);
        groupeEtudiantJpaRepository.flush();
        return true;
    }

    private void setGroupeEtudiantData(GroupeEtudiant groupeEtudiant, GroupeEtudiantDto groupeEtudiantDto) {

    }
}