package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.dto.*;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.AppSignatureEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConventionService;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.esup_portail.esup_stage.service.signature.SignatureService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

@ApiController
@RequestMapping("/conventions")
public class ConventionController {

    private static final Logger logger	= LogManager.getLogger(ConventionController.class);

    @Autowired
    ConventionRepository conventionRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    TypeConventionJpaRepository typeConventionJpaRepository;

    @Autowired
    LangueConventionJpaRepository langueConventionJpaRepository;

    @Autowired
    PaysJpaRepository paysJpaRepository;
    @Autowired
    ThemeJpaRepository themeJpaRepository;
    @Autowired
    TempsTravailJpaRepository tempsTravailJpaRepository;
    @Autowired
    UniteGratificationJpaRepository uniteGratificationJpaRepository;
    @Autowired
    UniteDureeJpaRepository uniteDureeJpaRepository;
    @Autowired
    DeviseJpaRepository deviseJpaRepository;
    @Autowired
    ModeVersGratificationJpaRepository modeVersGratificationJpaRepository;
    @Autowired
    OrigineStageJpaRepository origineStageJpaRepository;
    @Autowired
    NatureTravailJpaRepository natureTravailJpaRepository;
    @Autowired
    ModeValidationStageJpaRepository modeValidationStageJpaRepository;
    @Autowired
    StructureJpaRepository structureJpaRepository;
    @Autowired
    ServiceJpaRepository serviceJpaRepository;
    @Autowired
    ContactJpaRepository contactJpaRepository;
    @Autowired
    EnseignantJpaRepository enseignantJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    HistoriqueValidationJpaRepository historiqueValidationJpaRepository;

    @Autowired
    AvenantJpaRepository avenantJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    ImpressionService impressionService;

    @Autowired
    ConventionService conventionService;

    @Autowired
    SignatureService signatureService;

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Convention> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        filters = addUserContextFilter(filters);

        PaginatedResponse<Convention> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(conventionRepository.count(filters));
        paginatedResponse.setData(conventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        filters = addUserContextFilter(filters);
        byte[] bytes = conventionRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        filters = addUserContextFilter(filters);
        StringBuilder csv = conventionRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
    }

    @GetMapping("/brouillon")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public Convention getBrouillon() {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        Convention convention = conventionJpaRepository.findBrouillon(utilisateur.getLogin());
        if (convention == null) {
            convention = new Convention();
        }
        return convention;
    }

    @GetMapping("/annees")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<AnneeUniversitaireDto> getListAnnees() {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        List<AnneeUniversitaireDto> results = new ArrayList<>();
        List<String> annees;
        if (!UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            if (UtilisateurHelper.isRole(utilisateur, Role.RESP_GES) || UtilisateurHelper.isRole(utilisateur, Role.GES)) {
                annees = conventionJpaRepository.getGestionnaireAnnees(utilisateur.getUid());
            } else if (UtilisateurHelper.isRole(utilisateur, Role.ENS)) {
                annees = conventionJpaRepository.getEnseignantAnnees(utilisateur.getUid());
            } else if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                annees = conventionJpaRepository.getEtudiantAnnees(utilisateur.getUid());
            } else {
                annees = conventionJpaRepository.getAnnees();
            }
        } else {
            annees = conventionJpaRepository.getAnnees();
        }
        String anneeEnCours = appConfigService.getAnneeUniv();
        for (String anneeLibelle : annees) {
            String annee = appConfigService.getAnneeUnivFromLibelle(anneeLibelle);
            AnneeUniversitaireDto anneeUniversitaireDto = new AnneeUniversitaireDto(annee, anneeLibelle);
            if (annee.equals(anneeEnCours)) {
                anneeUniversitaireDto.setAnneeEnCours(true);
            }
            results.add(anneeUniversitaireDto);
        }
        if (results.stream().noneMatch(AnneeUniversitaireDto::isAnneeEnCours)) {
            results.add(new AnneeUniversitaireDto(anneeEnCours, appConfigService.getAnneeUnivLibelle(anneeEnCours), true));
        }
        return results;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public Convention getById(@PathVariable("id") int id) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        // Pour les étudiants on vérifie que c'est une de ses conventions
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(convention.getEtudiant().getIdentEtudiant())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        conventionService.canViewEditConvention(convention, ServiceContext.getUtilisateur());
        return convention;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.CREATION})
    public Convention create(@Valid @RequestBody ConventionFormDto conventionFormDto) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        // Création d'un brouillon uniquement s'il n'y en a pas un déjà existant
        if (conventionJpaRepository.findBrouillon(utilisateur.getLogin()) != null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Vous avez déjà une convention en cours de création. Veuillez rafraichir la page.");
        }
        Convention convention = new Convention();
        convention.setNomenclature(new ConventionNomenclature());
        convention.setValidationCreation(false);
        conventionService.setConventionData(convention, conventionFormDto);
        convention = conventionJpaRepository.saveAndFlush(convention);
        return convention;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = AppFonctionEnum.CONVENTION, droits = {DroitEnum.MODIFICATION})
    public Convention update(@PathVariable("id") int id, @Valid @RequestBody ConventionFormDto conventionFormDto) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        Convention convention = conventionJpaRepository.findById(id);
        conventionService.setConventionData(convention, conventionFormDto);
        if (convention.isValidationCreation()) {
            convention.setValeurNomenclature();
        }
        convention = conventionJpaRepository.saveAndFlush(convention);

        if (convention.isValidationCreation()) {
            if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
                boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isModificationConventionEtudiant();
                boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isModificationConventionEtudiant();
                boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isModificationConventionEtudiant();
                boolean sendMailRespGestionnaire = configAlerteMailDto.getAlerteRespGestionnaire().isModificationConventionEtudiant();
                conventionService.sendValidationMail(convention, null, utilisateur,TemplateMail.CODE_ETU_MODIF_CONVENTION, sendMailEtudiant, sendMailEnseignant, sendMailGestionnaire, sendMailRespGestionnaire);
            }
            else if (UtilisateurHelper.isRole(utilisateur, Role.GES)) {
                ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
                boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isModificationConventionGestionnaire();
                boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isModificationConventionGestionnaire();
                boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isModificationConventionGestionnaire();
                boolean sendMailRespGestionnaire = configAlerteMailDto.getAlerteRespGestionnaire().isModificationConventionGestionnaire();
                conventionService.sendValidationMail(convention, null, utilisateur,TemplateMail.CODE_GES_MODIF_CONVENTION, sendMailEtudiant, sendMailEnseignant, sendMailGestionnaire, sendMailRespGestionnaire);
            }
        }
        return convention;
    }

    @PatchMapping("/{id}")
    @Secure(fonctions = AppFonctionEnum.CONVENTION, droits = {DroitEnum.MODIFICATION})
    public Convention singleFieldUpdate(@PathVariable("id") int id, @Valid @RequestBody ConventionSingleFieldDto conventionSingleFieldDto) {
        Convention convention = conventionJpaRepository.findById(id);
        // Pour les étudiants on vérifie que c'est une de ses conventions
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (convention == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(convention.getEtudiant().getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        setSingleFieldData(convention, conventionSingleFieldDto, utilisateur);
        if (convention.isValidationCreation()) {
            convention.setValeurNomenclature();
        }
        convention = conventionJpaRepository.saveAndFlush(convention);
        return convention;
    }

    @GetMapping("/{annee}/en-attente-validation-alerte")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE}, forbiddenEtu = true)
    public int countConventionEnAttente(@PathVariable("annee") String annee) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        List<Convention> conventions;
        boolean isEnseignant = false;
        // Récupération des conventions en attente de validation, pédagogique pour les enseignants, administrative pour les gestionnaires
        if (UtilisateurHelper.isRole(utilisateur, Role.ENS)) {
            conventions = conventionJpaRepository.getConventionEnAttenteEnseignant(appConfigService.getAnneeUnivLibelle(annee), utilisateur.getUid());
            isEnseignant = true;
        } else if (!UtilisateurHelper.isRole(utilisateur, Role.ADM) && (UtilisateurHelper.isRole(utilisateur, Role.RESP_GES) || UtilisateurHelper.isRole(utilisateur, Role.GES))) {
            conventions = conventionJpaRepository.getConventionEnAttenteGestionnaire(appConfigService.getAnneeUnivLibelle(annee), utilisateur.getUid());
        } else {
            conventions = conventionJpaRepository.getConventionEnAttenteGestionnaire(appConfigService.getAnneeUnivLibelle(annee));
        }
        int count = 0;
        for (Convention convention : conventions) {
            CentreGestion centreGestion = convention.getCentreGestion();
            if (centreGestion != null) {
                boolean enAttenteValidation = true;
                // En fonction de l'ordre de validation, on regarde si l'éventuelle validation précédente est passée
                Integer ordreValidation = isEnseignant ? centreGestion.getValidationPedagogiqueOrdre() : centreGestion.getValidationConventionOrdre();
                if (ordreValidation != null && ordreValidation > 1) { // Il y a une validation précédente
                    if (centreGestion.getValidationPedagogiqueOrdre() != null && centreGestion.getValidationPedagogiqueOrdre() <= (ordreValidation - 1)) {
                        enAttenteValidation = convention.getValidationPedagogique() != null && convention.getValidationPedagogique();
                    }
                    if (centreGestion.getVerificationAdministrativeOrdre() != null && centreGestion.getVerificationAdministrativeOrdre() <= (ordreValidation - 1)) {
                        enAttenteValidation = convention.getVerificationAdministrative() != null && convention.getVerificationAdministrative();
                    }
                    if (centreGestion.getValidationConventionOrdre() != null && centreGestion.getValidationConventionOrdre() <= (ordreValidation - 1)) {
                        enAttenteValidation = convention.getValidationConvention() != null && convention.getValidationConvention();
                    }
                }
                if (enAttenteValidation && convention.isDepasseDelaiValidation()) {
                    count++;
                }
            }

        }
        return count;
    }

    @PatchMapping("/validation-creation/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public Convention validationCreation(@PathVariable("id") int id) {
        Convention convention = conventionJpaRepository.findById(id);

        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        // Pour les étudiants on vérifie que c'est une de ses conventions
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(convention.getEtudiant().getIdentEtudiant())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }

        // Contrôle chevauchement de dates
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            if (convention.getDateDebutStage() != null && convention.getDateFinStage() != null && conventionJpaRepository.findDatesChevauchent(convention.getEtudiant().getIdentEtudiant(), convention.getId(), convention.getDateDebutStage(), convention.getDateFinStage()).size() > 0) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Les dates de début et fin de stage se chevauchent avec une de vos conventions");
            }
        }

        convention.setValidationCreation(true);
        convention.setDateValidationCreation(new Date());
        convention.setValeurNomenclature();
        convention = conventionJpaRepository.saveAndFlush(convention);

        if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
            boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isCreationConventionEtudiant();
            boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isCreationConventionGestionnaire();
            boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isCreationConventionEtudiant();
            boolean sendMailRespGestionnaire = configAlerteMailDto.getAlerteRespGestionnaire().isCreationConventionEtudiant();
            conventionService.sendValidationMail(convention, null, utilisateur,TemplateMail.CODE_ETU_CREA_CONVENTION, sendMailEtudiant, sendMailEnseignant, sendMailGestionnaire, sendMailRespGestionnaire); //ICI : ATTENTION IL Y AVAIT DEJA UN SENDMAILGESTIONNAIRE EN PARAM2 DE LA FONCTION
        }
        else if (UtilisateurHelper.isRole(utilisateur, Role.GES)) {
            ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
            boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isCreationConventionGestionnaire();
            boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isCreationConventionGestionnaire();
            boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isCreationConventionGestionnaire();
            boolean sendMailRespGestionnaire = configAlerteMailDto.getAlerteRespGestionnaire().isCreationConventionGestionnaire();
            conventionService.sendValidationMail(convention, null, utilisateur,TemplateMail.CODE_GES_CREA_CONVENTION, sendMailEtudiant, sendMailEnseignant, sendMailGestionnaire, sendMailRespGestionnaire);
        }
        return convention;
    }

    @PostMapping("/validation-administrative")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.VALIDATION})
    public int validationAdministrativeMultiple(@RequestBody IdsListDto idsListDto) {
        // Un enseignant n'a les droits que sur la validation pédagogique
        if (UtilisateurHelper.isRole(ServiceContext.getUtilisateur(), Role.ENS)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Type de validation inconnu");
        }
        if (idsListDto.getIds().size() == 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La liste est vide");
        }
        ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
        int count = 0;
        for (int id : idsListDto.getIds()) {
            Convention convention = conventionJpaRepository.findById(id);
            // On ne traite pas les convention déjà validée administrativement
            if (convention == null || (convention.getValidationConvention() != null && convention.getValidationConvention())) {
                continue;
            }
            validationAdministrative(convention, configAlerteMailDto, ServiceContext.getUtilisateur(), true);
            conventionService.validationAutoDonnees(convention, ServiceContext.getUtilisateur());
            count++;
        }
        return count;
    }

    @PatchMapping("/{id}/valider/{type}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.VALIDATION})
    public Convention validate(@PathVariable("id") int id, @PathVariable("type") String type) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        // Un enseignant n'a les droits que sur la validation pédagogique
        if (UtilisateurHelper.isRole(ServiceContext.getUtilisateur(), Role.ENS) && !type.equals("validationPedagogique")) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Type de validation inconnu");
        }
        switch (type) {
            case "validationPedagogique":
                validationPedagogique(convention, appConfigService.getConfigAlerteMail(), ServiceContext.getUtilisateur(), true);
                break;
            case "verificationAdministrative":
                verificationAdministrative(convention, appConfigService.getConfigAlerteMail(), ServiceContext.getUtilisateur(), true);
                break;
            case "validationConvention":
                validationAdministrative(convention, appConfigService.getConfigAlerteMail(), ServiceContext.getUtilisateur(), true);
                break;
            default:
                throw new AppException(HttpStatus.BAD_REQUEST, "Type de validation inconnu");
        }
        conventionService.validationAutoDonnees(convention, ServiceContext.getUtilisateur());
        return convention;
    }

    @PatchMapping("/{id}/devalider/{type}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.VALIDATION})
    public Convention unvalidate(@PathVariable("id") int id, @PathVariable("type") String type) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        if (convention.getDocumentId() != null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Convention déjà envoyée pour signature");
        }
        // Un enseignant n'a les droits que sur la validation pédagogique
        if (UtilisateurHelper.isRole(ServiceContext.getUtilisateur(), Role.ENS) && !type.equals("validationPedagogique")) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Type de validation inconnu");
        }
        switch (type) {
            case "validationPedagogique":
                validationPedagogique(convention, appConfigService.getConfigAlerteMail(), ServiceContext.getUtilisateur(), false);
                break;
            case "verificationAdministrative":
                verificationAdministrative(convention, appConfigService.getConfigAlerteMail(), ServiceContext.getUtilisateur(), false);
                break;
            case "validationConvention":
                validationAdministrative(convention, appConfigService.getConfigAlerteMail(), ServiceContext.getUtilisateur(), false);
                break;
            default:
                throw new AppException(HttpStatus.BAD_REQUEST, "Type de validation inconnu");
        }
        convention = conventionJpaRepository.saveAndFlush(convention);
        return convention;
    }

    @GetMapping("/{id}/historique-validations")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.VALIDATION})
    public List<HistoriqueValidation> getHistoriqueValidations(@PathVariable("id") int idConvention) {
        return historiqueValidationJpaRepository.findByConvention(idConvention);
    }

    @GetMapping("/{id}/pdf-convention")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> getConventionPDF(@PathVariable("id") int id, @RequestParam(name = "isRecap", required = false) boolean isRecap) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        if (convention.getNomEtabRef() == null || convention.getAdresseEtabRef() == null) {
            CentreGestion centreGestionEtab = centreGestionJpaRepository.getCentreEtablissement();
            // Erreur si le centre de type etablissement est null
            if (centreGestionEtab == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Centre de gestion de type établissement non trouvé");
            }
            convention.setNomEtabRef(centreGestionEtab.getNomCentre());
            convention.setAdresseEtabRef(centreGestionEtab.getAdresseComplete());
            conventionJpaRepository.saveAndFlush(convention);
        }
        ByteArrayOutputStream ou = new ByteArrayOutputStream();
        impressionService.generateConventionAvenantPDF(convention, null, ou, isRecap);

        byte[] pdf = ou.toByteArray();
        return ResponseEntity.ok().body(pdf);
    }

    @GetMapping("/{id}/pdf-avenant")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> getAvenantPDF(@PathVariable("id") int id) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
        }
        ByteArrayOutputStream ou = new ByteArrayOutputStream();
        impressionService.generateConventionAvenantPDF(avenant.getConvention(), avenant, ou, false);

        byte[] pdf = ou.toByteArray();
        return ResponseEntity.ok().body(pdf);
    }

    @DeleteMapping("/brouillon")
    @Secure
    public void deleteBrouillon() {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        Convention brouillon = conventionJpaRepository.findBrouillon(utilisateur.getLogin());
        if (brouillon != null) {
            conventionJpaRepository.delete(brouillon);
        }
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public Convention delete(@PathVariable("id") int id) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        // Pour les étudiants on vérifie que c'est une de ses conventions
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(convention.getEtudiant().getIdentEtudiant())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        conventionService.canViewEditConvention(convention, ServiceContext.getUtilisateur());
        // On n'autorise la suppression d'une convention si elle n'a aucune validation
        boolean hasValidation = false;
        if (convention.getCentreGestion().getValidationConvention() == true && convention.getValidationConvention() == true) {
            hasValidation = true;
        }
        if (convention.getCentreGestion().getValidationConvention() == true && convention.getValidationConvention() == true) {
            hasValidation = true;
        }
        if (convention.getCentreGestion().getValidationPedagogique() == true && convention.getValidationPedagogique() == true) {
            hasValidation = true;
        }
        if (convention.getCentreGestion().getVerificationAdministrative() == true && convention.getVerificationAdministrative() == true) {
            hasValidation = true;
        }
        if (hasValidation) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La convention a déjà été validée et ne peut être supprimée");
        }
        conventionJpaRepository.delete(convention);
        return convention;
    }

    @PostMapping("/signature-electronique")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.VALIDATION})
    public int envoiSignatureElectroniqueMultiple(@RequestBody IdsListDto idsListDto) {
        return signatureService.upload(idsListDto, false);
    }

    @PostMapping("/{id}/controle-signature-electronique")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.VALIDATION})
    public ResponseDto controleSignatureElectronique(@PathVariable("id") int id) {
        if (!appConfigService.getConfigGenerale().isSignatureEnabled()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La signature électronique n'est pas configurée");
        }
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        if (applicationBootstrap.getAppConfig().getAppSignatureEnabled() == AppSignatureEnum.DOCAPOSTE && convention.getCentreGestion().getCircuitSignature() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Le centre de gestion " + convention.getCentreGestion().getNomCentre() + " n'a pas de circuit de signature");
        }
        return conventionService.controleEmailTelephone(convention);
    }

    @PostMapping("/{id}/update-signature-electronique-info")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.VALIDATION})
    public Convention updateSignatureElectroniqueInfo(@PathVariable("id") int id) {
        if (!appConfigService.getConfigGenerale().isSignatureEnabled()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La signature électronique n'est pas configurée");
        }
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        signatureService.updateHistorique(convention);
        return convention;
    }

    private void setSingleFieldData(Convention convention, ConventionSingleFieldDto conventionSingleFieldDto, Utilisateur utilisateur) {
        conventionService.canViewEditConvention(convention, ServiceContext.getUtilisateur());
        if (!conventionService.isConventionModifiable(convention, ServiceContext.getUtilisateur())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La convention n'est plus modifiable");
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "codeLangueConvention")){
            LangueConvention langueConvention = langueConventionJpaRepository.findByCode((String) conventionSingleFieldDto.getValue());
            if (langueConvention == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "LangueConvention non trouvé");
            }
            convention.setLangueConvention(langueConvention);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idPays")){
            Pays pays = paysJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (pays == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Pays non trouvé");
            }
            convention.setPaysConvention(pays);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idTypeConvention")){
            TypeConvention typeConvention = typeConventionJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (typeConvention == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "TypeConvention non trouvé");
            }
            convention.setTypeConvention(typeConvention);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idTheme")){
            Theme theme = themeJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (theme == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Theme non trouvé");
            }
            convention.setTheme(theme);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "sujetStage")){
            convention.setSujetStage((String) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "competences")){
            convention.setCompetences((String) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "fonctionsEtTaches")){
            convention.setFonctionsEtTaches((String) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "details")){
            convention.setDetails((String) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "dateDebutStage")){
            Instant instant = Instant.parse((String) conventionSingleFieldDto.getValue()) ;
            convention.setDateDebutStage(java.util.Date.from(instant));
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "dateFinStage")){
            Instant instant = Instant.parse((String) conventionSingleFieldDto.getValue()) ;
            convention.setDateFinStage(java.util.Date.from(instant));
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "interruptionStage")){
            convention.setInterruptionStage((Boolean) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "horairesReguliers")){
            convention.setHorairesReguliers((Boolean) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "nbHeuresHebdo")){
            convention.setNbHeuresHebdo(conventionSingleFieldDto.getValue().toString());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "nbConges")){
            convention.setNbConges(conventionSingleFieldDto.getValue().toString());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "dureeExceptionnelle")){
            convention.setDureeExceptionnelle(conventionSingleFieldDto.getValue().toString());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idTempsTravail")){
            TempsTravail tempsTravail = tempsTravailJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (tempsTravail == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "TempsTravail non trouvé");
            }
            convention.setTempsTravail(tempsTravail);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "commentaireDureeTravail")){
            convention.setCommentaireDureeTravail((String) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "gratificationStage")){
            convention.setGratificationStage((Boolean) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "montantGratification")){
            convention.setMontantGratification((String) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idUniteGratification")){
            UniteGratification uniteGratification = uniteGratificationJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (uniteGratification == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "UniteGratification non trouvé");
            }
            convention.setUniteGratification(uniteGratification);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idUniteDuree")){
            UniteDuree uniteDuree = uniteDureeJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (uniteDuree == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "UniteDuree non trouvé");
            }
            convention.setUniteDureeGratification(uniteDuree);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idDevise")){
            Devise devise = deviseJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (devise == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Devise non trouvé");
            }
            convention.setDevise(devise);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idModeVersGratification")){
            ModeVersGratification modeVersGratification = modeVersGratificationJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (modeVersGratification == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "ModeVersGratification non trouvé");
            }
            convention.setModeVersGratification(modeVersGratification);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idOrigineStage")){
            OrigineStage origineStage = origineStageJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (origineStage == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "OrigineStage non trouvé");
            }
            convention.setOrigineStage(origineStage);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idNatureTravail")){
            NatureTravail natureTravail = natureTravailJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (natureTravail == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "NatureTravail non trouvé");
            }
            convention.setNatureTravail(natureTravail);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idModeValidationStage")){
            ModeValidationStage modeValidationStage = modeValidationStageJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (modeValidationStage == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "ModeValidationStage non trouvé");
            }
            convention.setModeValidationStage(modeValidationStage);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "modeEncadreSuivi")){
            convention.setModeEncadreSuivi((String) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "avantagesNature")){
            convention.setAvantagesNature((String) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "travailNuitFerie")){
            convention.setTravailNuitFerie((String) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "confidentiel")){
            convention.setConfidentiel((Boolean) conventionSingleFieldDto.getValue());
        }

        if (Objects.equals(conventionSingleFieldDto.getField(), "idStructure")){
            int oldIdStructure = convention.getStructure() != null ? convention.getStructure().getId() : 0;
            Structure structure = structureJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (structure == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Structure non trouvé");
            }
            convention.setStructure(structure);
            //Cascade structure change to relevant fields
            if (oldIdStructure != structure.getId()) {
                convention.setService(null);
                convention.setContact(null);
                convention.setSignataire(null);
            }
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idService")){
            int oldIdService = convention.getService() != null ? convention.getService().getId() : 0;
            Service service = serviceJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (service == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Service non trouvé");
            }
            convention.setService(service);
            //Cascade service change to relevant fields
            if (oldIdService != service.getId()) {
                convention.setContact(null);
                convention.setSignataire(null);
            }
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idContact")){
            Contact contact = contactJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (contact == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouvé");
            }
            convention.setContact(contact);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idEnseignant")){
            Enseignant enseignant = enseignantJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (enseignant == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Enseignant non trouvé");
            }
            convention.setEnseignant(enseignant);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idSignataire")){
            Contact signataire = contactJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (signataire == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Signataire non trouvé");
            }
            convention.setSignataire(signataire);
        }

        // Contrôle chevauchement de dates
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            if (convention.getDateDebutStage() != null && convention.getDateFinStage() != null && conventionJpaRepository.findDatesChevauchent(convention.getEtudiant().getIdentEtudiant(), convention.getId(), convention.getDateDebutStage(), convention.getDateFinStage()).size() > 0) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Les dates de début et fin de stage se chevauchent avec une de vos conventions");
            }
        }

    }

    @PostMapping("/{id}/controle-chevauchement")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public boolean isChevauchement(@PathVariable("id") int id, @RequestBody DateStageDto dateStageDto) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (!UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            return false;
        }
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        // Pour les étudiants on vérifie que c'est une de ses conventions
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(convention.getEtudiant().getIdentEtudiant())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        conventionService.canViewEditConvention(convention, ServiceContext.getUtilisateur());

        // Contrôle chevauchement de dates
        if (dateStageDto.getDateDebut() != null && dateStageDto.getDateFin() != null && conventionJpaRepository.findDatesChevauchent(convention.getEtudiant().getIdentEtudiant(), convention.getId(), dateStageDto.getDateDebut(), dateStageDto.getDateFin()).size() > 0) {
            return true;
        }
        return false;
    }

    private void validationPedagogique(Convention convention, ConfigAlerteMailDto configAlerteMailDto, Utilisateur utilisateurContext, boolean valider) {
        boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isValidationPedagogiqueConvention();
        boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isValidationPedagogiqueConvention();
        boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isValidationPedagogiqueConvention();
        boolean sendMailRespGestionnaire = configAlerteMailDto.getAlerteRespGestionnaire().isValidationPedagogiqueConvention();

        HistoriqueValidation historique = new HistoriqueValidation();
        historique.setValeurAvant(convention.getValidationPedagogique());
        historique.setLogin(utilisateurContext.getLogin());
        historique.setType("validationPedagogique");
        historique.setConvention(convention);

        convention.setValidationPedagogique(valider);
        if (valider) {
            convention.setLoginValidation(utilisateurContext.getLogin());
        }
        convention.setValeurNomenclature();
        convention = conventionJpaRepository.saveAndFlush(convention);

        historique.setValeurApres(valider);
        historiqueValidationJpaRepository.saveAndFlush(historique);

        conventionService.sendValidationMail(convention, null, utilisateurContext, valider ? TemplateMail.CODE_CONVENTION_VALID_PEDAGOGIQUE : TemplateMail.CODE_CONVENTION_DEVALID_PEDAGOGIQUE, sendMailEtudiant, sendMailEnseignant, sendMailGestionnaire, sendMailRespGestionnaire);
    }

    private void verificationAdministrative(Convention convention, ConfigAlerteMailDto configAlerteMailDto, Utilisateur utilisateurContext, boolean valider) {
        boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isVerificationAdministrativeConvention();
        boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isVerificationAdministrativeConvention();
        boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isVerificationAdministrativeConvention();
        boolean sendMailRespGestionnaire = configAlerteMailDto.getAlerteRespGestionnaire().isVerificationAdministrativeConvention();

        HistoriqueValidation historique = new HistoriqueValidation();
        historique.setValeurAvant(convention.getVerificationAdministrative());
        historique.setLogin(utilisateurContext.getLogin());
        historique.setType("verificationAdministrative");
        historique.setConvention(convention);

        convention.setVerificationAdministrative(valider);
        convention.setValeurNomenclature();
        convention = conventionJpaRepository.saveAndFlush(convention);

        historique.setValeurApres(valider);
        historiqueValidationJpaRepository.saveAndFlush(historique);
        conventionService.sendValidationMail(convention, null, utilisateurContext, valider ? TemplateMail.CODE_CONVENTION_VERIF_ADMINISTRATIVE : TemplateMail.CODE_CONVENTION_DEVERIF_ADMINISTRATIVE, sendMailEtudiant, sendMailEnseignant, sendMailGestionnaire, sendMailRespGestionnaire);
    }

    private void validationAdministrative(Convention convention, ConfigAlerteMailDto configAlerteMailDto, Utilisateur utilisateurContext, boolean valider) {
        boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isValidationAdministrativeConvention();
        boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isValidationAdministrativeConvention();
        boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isValidationAdministrativeConvention();
        boolean sendMailRespGestionnaire = configAlerteMailDto.getAlerteRespGestionnaire().isValidationAdministrativeConvention();

        HistoriqueValidation historique = new HistoriqueValidation();
        historique.setValeurAvant(convention.getValidationConvention());
        historique.setLogin(utilisateurContext.getLogin());
        historique.setType("validationConvention");
        historique.setConvention(convention);

        convention.setValidationConvention(valider);
        if (valider) {
            convention.setLoginValidation(utilisateurContext.getLogin());
        }
        convention.setValeurNomenclature();
        convention = conventionJpaRepository.saveAndFlush(convention);

        historique.setValeurApres(valider);
        historiqueValidationJpaRepository.saveAndFlush(historique);
        conventionService.sendValidationMail(convention, null, utilisateurContext, valider ? TemplateMail.CODE_CONVENTION_VALID_ADMINISTRATIVE : TemplateMail.CODE_CONVENTION_DEVALID_ADMINISTRATIVE, sendMailEtudiant, sendMailEnseignant, sendMailGestionnaire, sendMailRespGestionnaire);
    }

    private String addUserContextFilter(String filters) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (!UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            JSONObject jsonFilters = new JSONObject(filters);
            Map<String, Object> currentUser = new HashMap<>();
            currentUser.put("type", "text");
            currentUser.put("value", utilisateur.getUid());
            currentUser.put("specific", true);
            if (UtilisateurHelper.isRole(utilisateur, Role.RESP_GES) || UtilisateurHelper.isRole(utilisateur, Role.GES)) {
                Map<String, Object> ges = new HashMap<>();
                ges.put("type", "text");
                ges.put("value", utilisateur.getLogin());
                ges.put("specific", true);
                jsonFilters.put("centreGestion.personnels", ges);
            } else if (UtilisateurHelper.isRole(utilisateur, Role.ENS)) {
                jsonFilters.put("enseignant.uidEnseignant", currentUser);
            } else if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                jsonFilters.put("etudiant.identEtudiant", currentUser);
            }

            filters = jsonFilters.toString();
        }
        return filters;
    }

    @GetMapping("/{id}/download-signed-doc")
    @Secure
    public ResponseEntity<byte[]> downloadDoc(@PathVariable("id") int id) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvé");
        }
        MetadataDto metadataDto = signatureService.getPublicMetadata(convention);
        String filePath = signatureService.getSignatureFilePath(metadataDto.getTitle());
        if (Files.exists(Paths.get(filePath))) {
            try {
                return ResponseEntity.ok().body(FileUtils.readFileToByteArray(new File(filePath)));
            } catch (IOException e) {
                logger.error("Erreur lors de la lecture du fichier", e);
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur sur le téléchargement du fichier");
            }
        } else {
            throw new AppException(HttpStatus.NOT_FOUND, "Fichier non trouvé");
        }
    }
}
