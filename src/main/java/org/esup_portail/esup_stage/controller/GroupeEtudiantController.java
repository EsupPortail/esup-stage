package org.esup_portail.esup_stage.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.dto.*;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConventionService;
import org.esup_portail.esup_stage.service.MailerService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.*;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApiController
@RequestMapping("/groupeEtudiant")
public class GroupeEtudiantController {

    private static final Logger logger	= LogManager.getLogger(ConsigneController.class);

    @Autowired
    GroupeEtudiantRepository groupeEtudiantRepository;

    @Autowired
    GroupeEtudiantJpaRepository groupeEtudiantJpaRepository;

    @Autowired
    HistoriqueMailGroupeJpaRepository historiqueMailGroupeJpaRepository;

    @Autowired
    EtudiantRepository etudiantRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    TypeConventionJpaRepository typeConventionJpaRepository;

    @Autowired
    EtudiantGroupeEtudiantJpaRepository etudiantGroupeEtudiantJpaRepository;

    @Autowired
    MailerService mailerService;

    @Autowired
    LangueConventionJpaRepository langueConventionJpaRepository;

    @Autowired
    StructureJpaRepository structureJpaRepository;

    @Autowired
    ImpressionService impressionService;

    @Autowired
    ApogeeService apogeeService;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    ConventionService conventionService;

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

    @GetMapping("/historique/{id}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<HistoriqueMailGroupe> getHistorique(@PathVariable("id") int id) {
        return historiqueMailGroupeJpaRepository.findByGroupeEtudiant(id);
    }

    @PatchMapping("/{id}/setTypeConventionGroupe/{typeConventionId}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public GroupeEtudiant setTypeConventionGroupe(@PathVariable("id") int id, @PathVariable("typeConventionId") int typeConventionId) {
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvée");
        }
        TypeConvention typeConvention = typeConventionJpaRepository.findById(typeConventionId);
        if (typeConvention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "TypeConvention non trouvé");
        }
        Convention groupeConvention = groupeEtudiant.getConvention();
        groupeConvention.setTypeConvention(typeConvention);
        conventionJpaRepository.save(groupeConvention);
        for (EtudiantGroupeEtudiant etudiant : groupeEtudiant.getEtudiantGroupeEtudiants()) {
            Convention etudiantConvention = etudiant.getConvention();
            etudiantConvention.setTypeConvention(typeConvention);
            conventionJpaRepository.save(etudiantConvention);
        }
        conventionJpaRepository.flush();
        return groupeEtudiant;
    }

    @PatchMapping("/{id}/setInfosStageValid/{valid}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public GroupeEtudiant setInfosStageValid(@PathVariable("id") int id, @PathVariable("valid") boolean valid) {
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvée");
        }
        groupeEtudiant.setInfosStageValid(valid);
        return groupeEtudiantJpaRepository.saveAndFlush(groupeEtudiant);
    }

    @PatchMapping("/{id}/validateBrouillon")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.CREATION})
    public GroupeEtudiant validateBrouillon(@PathVariable("id") int id) {
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvée");
        }
        groupeEtudiant.setValidationCreation(true);
        return groupeEtudiantJpaRepository.saveAndFlush(groupeEtudiant);
    }

    @PatchMapping("/{id}/mergeAndValidateConventions")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.VALIDATION})
    public GroupeEtudiant mergeAndValidateConventions(@PathVariable("id") int id) {
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvée");
        }
        Convention groupeConvention = groupeEtudiant.getConvention();
        for (EtudiantGroupeEtudiant etudiant : groupeEtudiant.getEtudiantGroupeEtudiants()){

            Convention etudiantConvention = etudiant.getConvention();
            Convention etudiantMergedConvention = new Convention();
            try {
                //applications des champs par défaults du groupe aux conventions de chaque étudiant quand ils n'ont pas de valeurs spécifiques pour ces champs
                mergeObjects(etudiantMergedConvention, etudiantConvention);
                mergeObjects(etudiantMergedConvention, groupeConvention);
            } catch (Exception e) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur création des conventions en masse");
            }
            etudiantMergedConvention.setCreationEnMasse(true);
            etudiantMergedConvention.setValidationCreation(true);
            etudiantMergedConvention.setDateValidationCreation(new Date());
            etudiantMergedConvention.setValidationPedagogique(true);
            etudiantMergedConvention.setVerificationAdministrative(true);
            etudiantMergedConvention.setValidationConvention(true);
            etudiantMergedConvention.setLoginValidation(ServiceContext.getUtilisateur().getLogin());
            conventionService.validationAutoDonnees(etudiantMergedConvention, ServiceContext.getUtilisateur());
            conventionJpaRepository.save(etudiantMergedConvention);

            Convention previousConvention = etudiant.getMergedConvention();
            etudiant.setMergedConvention(etudiantMergedConvention);
            etudiantGroupeEtudiantJpaRepository.save(etudiant);
            if(previousConvention != null)
                conventionJpaRepository.delete(previousConvention);
        }
        groupeEtudiant.setValidationCreation(true);
        return groupeEtudiantJpaRepository.saveAndFlush(groupeEtudiant);
    }

    public static <T> T mergeObjects(T first, T second) throws IllegalAccessException {
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
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findBrouillon(utilisateur.getLogin());
        return groupeEtudiant;
    }

    @GetMapping("/duplicate/{id}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public GroupeEtudiant duplicate(@PathVariable("id") int id) {

        //Suppression de l'ancien brouilon
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        GroupeEtudiant groupeEtudiantBrouillon = groupeEtudiantJpaRepository.findBrouillon(utilisateur.getLogin());

        if (groupeEtudiantBrouillon != null) {
            delete(groupeEtudiantBrouillon.getId());
        }

        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvée");
        }

        GroupeEtudiantDto groupeEtudiantDto = new GroupeEtudiantDto();
        groupeEtudiantDto.setCodeGroupe(groupeEtudiant.getCode() + " duplicate");
        groupeEtudiantDto.setNomGroupe(groupeEtudiant.getNom());

        List<String> numEtudiants = groupeEtudiant.getEtudiantGroupeEtudiants().stream().map(EtudiantGroupeEtudiant::getEtudiantNumEtudiant).collect(Collectors.toList());

        List<EtudiantDiplomeEtapeResponse> etudiantsAdded = new ArrayList<>();
        for(String numEtudiant : numEtudiants){
            EtudiantDiplomeEtapeResponse etudiant = new EtudiantDiplomeEtapeResponse();
            etudiant.setCodEtu(numEtudiant);
            etudiantsAdded.add(etudiant);
        }
        groupeEtudiantDto.setEtudiantAdded(etudiantsAdded);

        GroupeEtudiant newGroupeEtudiant = create(groupeEtudiantDto);
        newGroupeEtudiant.setInfosStageValid(true);

        //Duplication de la convention du groupe
        Convention oldGroupeConvention = groupeEtudiant.getConvention();
        Convention newGroupeConvention = newGroupeEtudiant.getConvention();
        try {
            newGroupeConvention = mergeObjects(newGroupeConvention, oldGroupeConvention);
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur création des conventions en masse");
        }
        conventionJpaRepository.save(newGroupeConvention);

        //Duplication des conventions des étudiants du groupe
        //for (EtudiantGroupeEtudiant etudiant : groupeEtudiant.getEtudiantGroupeEtudiants()) {
        //    Convention oldEtudiantConvention = etudiant.getConvention();
        //    for (EtudiantGroupeEtudiant newEtudiant : newGroupeEtudiant.getEtudiantGroupeEtudiants()) {
        //        Convention newEtudiantConvention = newEtudiant.getConvention();
        //        if(newEtudiantConvention.getId() == oldEtudiantConvention.getId()){
        //            try {
        //                newEtudiantConvention = mergeObjects(newEtudiantConvention, oldEtudiantConvention);
        //            } catch (Exception e) {
        //                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur création des conventions en masse");
        //            }
        //            conventionJpaRepository.save(newEtudiantConvention);
        //        }
        //    }
        //}

        return groupeEtudiantJpaRepository.saveAndFlush(groupeEtudiant);
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.CREATION})
    public GroupeEtudiant create(@Valid @RequestBody GroupeEtudiantDto groupeEtudiantDto) {
        // Erreur si code groupe déjà existant
        if (groupeEtudiantRepository.exists(groupeEtudiantDto.getCodeGroupe(), 0)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Code groupe déjà existant");
        }
        // Erreur si aucun étudiant
        if (groupeEtudiantDto.getEtudiantAdded().size() == 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Aucun étudiant à ajouter");
        }

        GroupeEtudiant groupeEtudiant = new GroupeEtudiant();

        //le premier étudiant de la liste est affecté à la convention du groupe d'étudiant
        EtudiantDiplomeEtapeResponse e = groupeEtudiantDto.getEtudiantAdded().get(0);
        Convention convention = createNewConvention(e);

        groupeEtudiant.setConvention(convention);
        groupeEtudiant.setCode(groupeEtudiantDto.getCodeGroupe());
        groupeEtudiant.setNom(groupeEtudiantDto.getNomGroupe());

        groupeEtudiant = groupeEtudiantJpaRepository.save(groupeEtudiant);

        List<EtudiantGroupeEtudiant> etudiantGroupeEtudiants = new ArrayList<>();

        for (EtudiantDiplomeEtapeResponse etudiant : groupeEtudiantDto.getEtudiantAdded()) {
            EtudiantGroupeEtudiant etudiantGroupeEtudiant = createNewEtudiantGroupeEtudiant(groupeEtudiant, etudiant);
            etudiantGroupeEtudiants.add(etudiantGroupeEtudiant);
        }
        groupeEtudiant.setEtudiantGroupeEtudiants(etudiantGroupeEtudiants);
        return groupeEtudiantJpaRepository.saveAndFlush(groupeEtudiant);
    }


    @PostMapping("/pdf-convention")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> getConventionPDF(@Valid @RequestBody IdsListDto idsListDto) {
        ByteArrayOutputStream archiveOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(archiveOutputStream);
        try {
            for(int conventionId : idsListDto.getIds()){

                Convention convention = conventionJpaRepository.findById(conventionId);
                if (convention == null) {
                    throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
                }

                ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
                impressionService.generateConventionAvenantPDF(convention, null, pdfOutputStream,false);

                byte[] pdf = pdfOutputStream.toByteArray();
                String filename = "Convention_" + convention.getEtudiant().getNom() + "_" +convention.getEtudiant().getPrenom() + ".pdf";
                ZipEntry entry = new ZipEntry(filename);
                zos.putNextEntry(entry);
                zos.write(pdf);
                zos.closeEntry();
            }
            zos.close();
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la création de l'achive zip");
        }

        byte[] archive = archiveOutputStream.toByteArray();
        return ResponseEntity.ok().body(archive);
    }

    @PostMapping("/sendMail/{id}/templateMail/{template}")
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public boolean sendMail(@PathVariable("id") int id, @PathVariable("template") String template, @Valid @RequestBody IdsListDto idsListDto) {

        GroupeEtudiant groupeEtudiant = groupeEtudiantJpaRepository.findById(id);
        if (groupeEtudiant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "GroupeEtudiant non trouvée");
        }
        HashMap<Structure,ArrayList<EtudiantGroupeEtudiant>> etudiantsByStructure = new HashMap<>();

        for(EtudiantGroupeEtudiant ege : groupeEtudiant.getEtudiantGroupeEtudiants()){
            if(idsListDto.getIds().contains(ege.getId())){
                Structure structure = ege.getMergedConvention().getStructure();
                if(!etudiantsByStructure.containsKey(structure)){
                    etudiantsByStructure.put(structure,new ArrayList<>());
                }
                etudiantsByStructure.get(structure).add(ege);
            }
        }

        for(Structure structure : etudiantsByStructure.keySet()){

            String mailto = structure.getMail();

            HistoriqueMailGroupe historique = new HistoriqueMailGroupe();
            historique.setDate(new Date());
            historique.setMailto(mailto);
            historique.setLogin(ServiceContext.getUtilisateur().getLogin());
            historique.setGroupeEtudiant(groupeEtudiant);

            historiqueMailGroupeJpaRepository.saveAndFlush(historique);

            try {
                ByteArrayOutputStream archiveOutputStream = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(archiveOutputStream);
                for(EtudiantGroupeEtudiant ege : etudiantsByStructure.get(structure)){

                    Convention convention = ege.getMergedConvention();

                    ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
                    impressionService.generateConventionAvenantPDF(convention, null, pdfOutputStream, false);

                    byte[] pdf = pdfOutputStream.toByteArray();
                    String filename = "Convention_" + ege.getEtudiant().getNom() + "_" +ege.getEtudiant().getPrenom() + ".pdf";
                    ZipEntry entry = new ZipEntry(filename);
                    zos.putNextEntry(entry);
                    zos.write(pdf);
                    zos.closeEntry();
                }
                zos.close();
                byte[] archive = archiveOutputStream.toByteArray();
                if (groupeEtudiant.getEtudiantGroupeEtudiants().get(0).getMergedConvention().getCentreGestion().isOnlyMailCentreGestion()) {
                    mailerService.sendMailGroupe(groupeEtudiant.getEtudiantGroupeEtudiants().get(0).getMergedConvention().getCentreGestion().getMail()
                            , groupeEtudiant.getEtudiantGroupeEtudiants().get(0).getMergedConvention(), ServiceContext.getUtilisateur(), template, archive);
                } else {
                    mailerService.sendMailGroupe(mailto, groupeEtudiant.getEtudiantGroupeEtudiants().get(0).getMergedConvention(), ServiceContext.getUtilisateur(), template, archive);
                }
            } catch (IOException e) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la création de l'achive zip");
            }
        }

        return true;
    }

    @PostMapping(value = "/import/{id}", consumes ="text/csv")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public void importStructures(InputStream inputStream,@PathVariable("id") int groupeId) {

        logger.info("import start");

        int indexNumEtu = 0;
        int indexNom = 1;
        int indexPrenom = 2;
        int indexRNE = 3;
        int indexSIRET = 4;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = "";
            String separator = ";";
            boolean isHeader = true;
            int lineno = 0;

            while ((line = br.readLine()) != null) {
                lineno++;
                if(isHeader){
                    isHeader = false;
                }else{

                    Structure structure;

                    String[] columns = line.split(separator, -1);
                    String RNE = columns[indexRNE];
                    if(!RNE.isEmpty()){
                        structure = structureJpaRepository.findByRNE(RNE);
                        if (structure == null) {
                            throw new AppException(HttpStatus.NOT_FOUND, "Aucune structure trouvée pour le RNE fournit : " +
                                    RNE + ", à la line : " + lineno);
                        }
                    }else{
                        String SIRET = columns[indexSIRET];
                        if(!SIRET.isEmpty()){
                            structure = structureJpaRepository.findBySiret(SIRET);
                            if (structure == null) {
                                throw new AppException(HttpStatus.NOT_FOUND, "Aucune structure trouvée pour le SIRET fournit : " +
                                        RNE + ", à la line : " + lineno);
                            }
                        }else{
                            throw new AppException(HttpStatus.NOT_FOUND, "Aucun numéro SIRET ou RNE fournit pour la line : " + lineno);
                        }
                    }
                    String numEtu = columns[indexNumEtu];
                    Etudiant etudiant = etudiantRepository.findByNumEtudiant(numEtu);
                    if (etudiant == null) {
                        throw new AppException(HttpStatus.NOT_FOUND, "Aucune etudiant trouvé pour le numero etudiant fournit : " +
                                numEtu + ", à la line : " + lineno);
                    }

                    EtudiantGroupeEtudiant ege = etudiantGroupeEtudiantJpaRepository.findByEtudiantAndGroupe(etudiant.getId(),groupeId);
                    if (ege == null) {
                        throw new AppException(HttpStatus.NOT_FOUND, "Aucune etudiant trouvé dans le groupe pour le numero " +
                                "etudiant fournit : " + numEtu + ", à la line : " + lineno);
                    }
                    Convention convention = ege.getConvention();
                    convention.setStructure(structure);
                    conventionJpaRepository.save(convention);
                }
            }
            conventionJpaRepository.flush();
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
        groupeEtudiant.setNom(groupeEtudiantDto.getNomGroupe());
        groupeEtudiant.setCode(groupeEtudiantDto.getCodeGroupe());

        List<String> oldEtudiants = groupeEtudiant.getEtudiantGroupeEtudiants().stream().map(EtudiantGroupeEtudiant::getEtudiantNumEtudiant).collect(Collectors.toList());
        List<EtudiantDiplomeEtapeResponse> newEtudiants = groupeEtudiantDto.getEtudiantAdded();

        List<EtudiantDiplomeEtapeResponse> addedEtudiants = new ArrayList<>();
        List<Integer> removedEtudiants = groupeEtudiantDto.getEtudiantRemovedIds();

        for(EtudiantDiplomeEtapeResponse etudiant : newEtudiants){
            if(!oldEtudiants.contains(etudiant.getCodEtu())){
                addedEtudiants.add(etudiant);
            }
        }

        List<EtudiantGroupeEtudiant> etudiantGroupeEtudiants = groupeEtudiant.getEtudiantGroupeEtudiants();

        //removed etudiants
        Iterator<EtudiantGroupeEtudiant> it = etudiantGroupeEtudiants.iterator();
        while(it.hasNext()) {
            EtudiantGroupeEtudiant etudiantGroupeEtudiant = it.next();
            if(removedEtudiants.contains(etudiantGroupeEtudiant.getId())){
                it.remove();
                etudiantGroupeEtudiantJpaRepository.delete(etudiantGroupeEtudiant);
                conventionJpaRepository.delete(etudiantGroupeEtudiant.getConvention());
            }
        }

        //added etudiants
        for(EtudiantDiplomeEtapeResponse etudiant : addedEtudiants){
            EtudiantGroupeEtudiant etudiantGroupeEtudiant = createNewEtudiantGroupeEtudiant(groupeEtudiant, etudiant);
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

    private EtudiantGroupeEtudiant createNewEtudiantGroupeEtudiant(GroupeEtudiant groupeEtudiant, EtudiantDiplomeEtapeResponse etudiant) {
        Convention convention = createNewConvention(etudiant);
        EtudiantGroupeEtudiant etudiantGroupeEtudiant = new EtudiantGroupeEtudiant();
        etudiantGroupeEtudiant.setEtudiant(convention.getEtudiant());
        etudiantGroupeEtudiant.setConvention(convention);
        etudiantGroupeEtudiant.setGroupeEtudiant(groupeEtudiant);
        return etudiantGroupeEtudiantJpaRepository.save(etudiantGroupeEtudiant);
    }

    private Convention createNewConvention(EtudiantDiplomeEtapeResponse etudiant) {
        EtudiantRef etudiantRef = apogeeService.getInfoApogee(etudiant.getCodEtu(), etudiant.getAnnee());
        ApogeeMap apogeeMap = apogeeService.getEtudiantEtapesInscription(etudiant.getCodEtu(), etudiant.getAnnee());
        RegimeInscription regIns = apogeeMap.getRegimeInscription().stream().filter(r -> r.getAnnee().equals(etudiant.getAnnee())).findAny().orElse(null);
        TypeConvention typeConvention = null;
        if (regIns != null) {
            typeConvention = typeConventionJpaRepository.findByCodeCtrl(regIns.getLicRegIns());
        }
        EtapeInscription etapeInscription = apogeeMap.getListeEtapeInscriptions().stream()
                .filter(i -> i.getCodeComposante().equals(etudiant.getCodeComposante())
                        && i.getCodeDiplome().equals(etudiant.getCodeDiplome())
                        && i.getVersionDiplome().equals(etudiant.getVersionDiplome())
                        && i.getCodeEtp().equals(etudiant.getCodeEtape())
                        && i.getCodVrsVet().equals(etudiant.getVersionEtape())
                )
                .findAny()
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Aucun inscription trouvée"));
        TypeConvention typeCesure = apogeeService.changeTypeConventionByCodeCursus(etapeInscription.getCodeCursusAmenage());
        if (typeCesure != null) {
            typeConvention = typeCesure;
        }
        if (typeConvention == null) {
            throw new AppException(HttpStatus.NO_CONTENT, "Type de convention non trouvé");
        }
        CentreGestion centreGestionEtab = conventionService.getCentreGestionEtab();
        ConventionFormationDto inscription = new ConventionFormationDto();
        inscription.setAnnee(etudiant.getAnnee());
        inscription.setEtapeInscription(etapeInscription);
        inscription.setCentreGestion(conventionService.getCentreGestion(centreGestionEtab, etudiant.getCodeComposante(), etudiant.getCodeEtape(), etudiant.getVersionEtape()));
        inscription.setTypeConvention(typeConvention);

        ConventionFormDto conventionFormDto = new ConventionFormDto();

        LangueConvention langueConvention = langueConventionJpaRepository.findByCode("fr");

        conventionFormDto.setIdTypeConvention(typeConvention.getId());
        conventionFormDto.setCodeLangueConvention(langueConvention.getCode());
        conventionFormDto.setCodeComposante(etapeInscription.getCodeComposante());
        conventionFormDto.setCodeEtape(etapeInscription.getCodeEtp());
        conventionFormDto.setCodeVersionEtape(etapeInscription.getCodVrsVet());
        conventionFormDto.setAnnee(inscription.getAnnee());
        conventionFormDto.setNumEtudiant(etudiant.getCodEtu());

        conventionFormDto.setAdresseEtudiant(etudiantRef.getMainAddress());
        conventionFormDto.setCodePostalEtudiant(etudiantRef.getPostalCode());
        conventionFormDto.setVilleEtudiant(etudiantRef.getTown());
        conventionFormDto.setPaysEtudiant(etudiantRef.getCountry());
        conventionFormDto.setTelEtudiant(etudiantRef.getPhone());
        conventionFormDto.setTelPortableEtudiant(etudiantRef.getPortablePhone());
        conventionFormDto.setCourrielPersoEtudiant(etudiantRef.getMailPerso());

        Convention convention = new Convention();
        convention.setValidationCreation(false);
        convention.setCreationEnMasse(true);
        conventionService.setConventionData(convention,conventionFormDto);

        return conventionJpaRepository.save(convention);
    }

}