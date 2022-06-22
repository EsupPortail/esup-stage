package org.esup_portail.esup_stage.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.dto.ContextDto;
import org.esup_portail.esup_stage.dto.GroupeEtudiantDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@ApiController
@RequestMapping("/groupeEtudiant")
public class GroupeEtudiantController {

    private static final Logger logger	= LogManager.getLogger(ConsigneController.class);

    @Autowired
    GroupeEtudiantRepository groupeEtudiantRepository;

    @Autowired
    GroupeEtudiantJpaRepository groupeEtudiantJpaRepository;

    @Autowired
    EtudiantJpaRepository etudiantJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    ConventionController conventionController;

    @Autowired
    EtudiantGroupeEtudiantJpaRepository etudiantGroupeEtudiantJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    TypeConventionJpaRepository typeConventionJpaRepository;

    @GetMapping
    @Secure
    public PaginatedResponse<GroupeEtudiant> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {

        PaginatedResponse<GroupeEtudiant> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(groupeEtudiantRepository.count(filters));
        paginatedResponse.setData(groupeEtudiantRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public GroupeEtudiant getById(@PathVariable("id") int id) {
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvée");
        }
        return groupeEtudiant;
    }

    @PatchMapping("/{id}/setInfosStageValid/{valid}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public GroupeEtudiant setInfosStageValid(@PathVariable("id") int id,@PathVariable("valid") boolean valid) {
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvée");
        }
        groupeEtudiant.setInfosStageValid(valid);
        return groupeEtudiantJpaRepository.saveAndFlush(groupeEtudiant);
    }

    @PatchMapping("/{id}/valider")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.VALIDATION})
    public GroupeEtudiant validate(@PathVariable("id") int id) {
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvée");
        }
        Convention groupeConvention = groupeEtudiant.getConvention();
        for (EtudiantGroupeEtudiant etudiant : groupeEtudiant.getEtudiantGroupeEtudiants()){

            Convention etudiantConvention = etudiant.getConvention();
            try {
                //appplications des champs par défaults du groupe aux conventions de chaque étudiant quand ils n'ont pas de valeurs spécifiques pour ces champs
                etudiantConvention = mergeObjects(etudiantConvention, groupeConvention);
            } catch (Exception e) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur création des conventions en masse");
            }
            etudiantConvention.setValidationCreation(true);
            etudiantConvention.setValidationPedagogique(true);
            etudiantConvention.setVerificationAdministrative(true);
            etudiantConvention.setValidationConvention(true);
            etudiantConvention.setLoginValidation(ServiceContext.getServiceContext().getUtilisateur().getLogin());
            conventionController.validationAutoDonnees(etudiantConvention, ServiceContext.getServiceContext().getUtilisateur());

            conventionJpaRepository.save(etudiantConvention);
        }
        groupeEtudiant.setValidationCreation(true);
        return groupeEtudiantJpaRepository.saveAndFlush(groupeEtudiant);
    }

    @SuppressWarnings("unchecked")
    public static <T> T mergeObjects(T first, T second) throws IllegalAccessException, InstantiationException {
        Class<?> clazz = first.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value1 = field.get(first);
            Object value2 = field.get(second);
            Object value = (value1 != null) ? value1 : value2;
            field.set(first, value);
        }
        return first;
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

    @PostMapping(value = "/import", consumes ="text/csv")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public void importStructures(InputStream inputStream) {

        logger.info("import start");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = "";
            String dataType = "";
            String separator = ";";
            boolean isHeader = false;

            while ((line = br.readLine()) != null) {

                logger.info("line : " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        List<Integer> oldEtudiants = groupeEtudiant.getEtudiantGroupeEtudiants().stream().map(EtudiantGroupeEtudiant::getEtudiantId).collect(Collectors.toList());
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
            if(!newEtudiants.contains(etudiantGroupeEtudiant.getEtudiantId())){
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
        convention.setValidationCreation(false);
        convention.setTypeConvention(typeConvention);
        convention.setCreationEnMasse(true);
        CentreGestion centreGestion = centreGestionJpaRepository.getCentreEtablissement();
        convention.setCentreGestion(centreGestion);
        return conventionJpaRepository.save(convention);
    }

}