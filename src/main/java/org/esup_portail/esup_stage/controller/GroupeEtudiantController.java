package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ContextDto;
import org.esup_portail.esup_stage.dto.GroupeEtudiantDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@ApiController
@RequestMapping("/groupeEtudiant")
public class GroupeEtudiantController {

    @Autowired
    GroupeEtudiantJpaRepository groupeEtudiantJpaRepository;

    @Autowired
    EtudiantJpaRepository etudiantJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    EtudiantGroupeEtudiantJpaRepository etudiantGroupeEtudiantJpaRepository;

    @Autowired
    TypeConventionJpaRepository typeConventionJpaRepository;

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
        return groupeEtudiant;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.CREATION})
    public GroupeEtudiant create(@Valid @RequestBody GroupeEtudiantDto groupeEtudiantDto) {
        GroupeEtudiant groupeEtudiant = new GroupeEtudiant();

        //le premier typeConvention est utilisé par valeur par défault (tentative)
        TypeConvention typeConvention = typeConventionJpaRepository.findAll().get(0);

        //le premier étudiant de la liste est affecté à la convention du groue d'étudiant (tentative)
        int id = groupeEtudiantDto.getEtudiantIds().get(0);
        Etudiant e = etudiantJpaRepository.findById(id);
        if (e == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Etudiant non trouvé");
        }
        Convention convention = createNewConvention(e,typeConvention);

        groupeEtudiant.setConvention(convention);
        groupeEtudiant.setNom(groupeEtudiantDto.getNomGroupe());

        groupeEtudiant = groupeEtudiantJpaRepository.save(groupeEtudiant);

        List<EtudiantGroupeEtudiant> etudiantGroupeEtudiants = new ArrayList<>();

        for(int etudiantId : groupeEtudiantDto.getEtudiantIds()){

            Etudiant etudiant = etudiantJpaRepository.findById(etudiantId);
            if (etudiant == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Etudiant non trouvé");
            }

            EtudiantGroupeEtudiant etudiantGroupeEtudiant = createNewEtudiantGroupeEtudiant(groupeEtudiant,etudiant,typeConvention);
            etudiantGroupeEtudiants.add(etudiantGroupeEtudiant);
        }
        groupeEtudiant.setEtudiantGroupeEtudiants(etudiantGroupeEtudiants);
        return groupeEtudiantJpaRepository.saveAndFlush(groupeEtudiant);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public GroupeEtudiant update(@PathVariable("id") int id, @Valid @RequestBody GroupeEtudiantDto groupeEtudiantDto) {
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvé");
        }
        //le premier typeConvention est utilisé par valeur par défault (tentative)
        TypeConvention typeConvention = typeConventionJpaRepository.findAll().get(0);
        groupeEtudiant.setNom(groupeEtudiantDto.getNomGroupe());

        List<Integer> oldEtudiants = groupeEtudiant.getEtudiantGroupeEtudiants().stream().map(EtudiantGroupeEtudiant::getId).collect(Collectors.toList());
        List<Integer> newEtudiants = groupeEtudiantDto.getEtudiantIds();

        List<Integer> addedEtudiants = new ArrayList<>();

        for(int etudiantId : newEtudiants){
            if(!oldEtudiants.contains(etudiantId)){
                addedEtudiants.add(etudiantId);
            }
        }

        List<EtudiantGroupeEtudiant> etudiantGroupeEtudiants = groupeEtudiant.getEtudiantGroupeEtudiants();

        //removed etudiants
        Iterator<EtudiantGroupeEtudiant> it = etudiantGroupeEtudiants.iterator();
        while(it.hasNext()) {
            EtudiantGroupeEtudiant etudiantGroupeEtudiant = it.next();
            if(!newEtudiants.contains(etudiantGroupeEtudiant.getId())){
                it.remove();
                etudiantGroupeEtudiantJpaRepository.delete(etudiantGroupeEtudiant);
            }
        }

        //added etudiants
        for(int etudiantId : addedEtudiants){

            Etudiant etudiant = etudiantJpaRepository.findById(etudiantId);
            if (etudiant == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Etudiant non trouvé");
            }

            EtudiantGroupeEtudiant etudiantGroupeEtudiant = createNewEtudiantGroupeEtudiant(groupeEtudiant,etudiant,typeConvention);
            etudiantGroupeEtudiants.add(etudiantGroupeEtudiant);
        }

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

    private EtudiantGroupeEtudiant createNewEtudiantGroupeEtudiant(GroupeEtudiant groupeEtudiant, Etudiant etudiant,TypeConvention typeConvention) {
        Convention convention = createNewConvention(etudiant,typeConvention);
        EtudiantGroupeEtudiant etudiantGroupeEtudiant = new EtudiantGroupeEtudiant();
        etudiantGroupeEtudiant.setEtudiant(etudiant);
        etudiantGroupeEtudiant.setConvention(convention);
        etudiantGroupeEtudiant.setGroupeEtudiant(groupeEtudiant);
        return etudiantGroupeEtudiantJpaRepository.save(etudiantGroupeEtudiant);
    }

    private Convention createNewConvention(Etudiant etudiant,TypeConvention typeConvention) {
        Convention convention = new Convention();
        convention.setEtudiant(etudiant);
        convention.setTypeConvention(typeConvention);
        return conventionJpaRepository.save(convention);
    }

}