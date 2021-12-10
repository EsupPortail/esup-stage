package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.*;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.MailerService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@ApiController
@RequestMapping("/conventions")
public class ConventionController {

    @Autowired
    ConventionRepository conventionRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    TypeConventionJpaRepository typeConventionJpaRepository;

    @Autowired
    LangueConventionJpaRepository langueConventionJpaRepository;

    @Autowired
    EtudiantJpaRepository etudiantJpaRepository;

    @Autowired
    EtapeJpaRepository etapeJpaRepository;

    @Autowired
    UfrJpaRepository ufrJpaRepository;

    @Autowired
    CritereGestionJpaRepository critereGestionJpaRepository;

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
    UtilisateurJpaRepository utilisateurJpaRepository;

    @Autowired
    HistoriqueValidationJpaRepository historiqueValidationJpaRepository;

    @Autowired
    ApogeeService apogeeService;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    MailerService mailerService;

    @Autowired
    ImpressionService impressionService;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Convention> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        ContextDto contexteDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contexteDto.getUtilisateur();
        if (!UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            JSONObject jsonFilters = new JSONObject(filters);
            Map<String, Object> currentUser = new HashMap<>();
            currentUser.put("type", "text");
            currentUser.put("value", utilisateur.getLogin());
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

        PaginatedResponse<Convention> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(conventionRepository.count(filters));
        paginatedResponse.setData(conventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/brouillon")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public Convention getBrouillon() {
        ContextDto contexteDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contexteDto.getUtilisateur();
        Convention convention = conventionJpaRepository.findBrouillon(utilisateur.getLogin());
        if (convention == null) {
            convention = new Convention();
        }
        return convention;
    }

    @GetMapping("/annees")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<AnneeUniversitaireDto> getListAnnees() {
        ContextDto contexteDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contexteDto.getUtilisateur();
        List<AnneeUniversitaireDto> results = new ArrayList<>();
        List<String> annees;
        if (!UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            if (UtilisateurHelper.isRole(utilisateur, Role.RESP_GES) || UtilisateurHelper.isRole(utilisateur, Role.GES)) {
                annees = conventionJpaRepository.getGestionnaireAnnees(utilisateur.getLogin());
            } else if (UtilisateurHelper.isRole(utilisateur, Role.ENS)) {
                annees = conventionJpaRepository.getEnseignantAnnees(utilisateur.getLogin());
            } else if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                annees = conventionJpaRepository.getEtudiantAnnees(utilisateur.getLogin());
            } else {
                annees = conventionJpaRepository.getAnnees(utilisateur.getLogin());
            }
        } else {
            annees = conventionJpaRepository.getAnnees(utilisateur.getLogin());
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
        canViewEditConvention(convention, ServiceContext.getServiceContext().getUtilisateur());
        return convention;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.CREATION})
    public Convention create(@Valid @RequestBody ConventionFormDto conventionFormDto) {
        Convention convention = new Convention();
        convention.setNomenclature(new ConventionNomenclature());
        convention.setValidationCreation(false);
        setConventionData(convention, conventionFormDto);
        convention = conventionJpaRepository.saveAndFlush(convention);
        return convention;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = AppFonctionEnum.CONVENTION, droits = {DroitEnum.MODIFICATION})
    public Convention update(@PathVariable("id") int id, @Valid @RequestBody ConventionFormDto conventionFormDto) {
        Utilisateur utilisateur = ServiceContext.getServiceContext().getUtilisateur();
        Convention convention = conventionJpaRepository.findById(id);
        setConventionData(convention, conventionFormDto);
        convention = conventionJpaRepository.saveAndFlush(convention);

        if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
            boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isModificationConventionEtudiant();
            boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isModificationConventionEtudiant();
            boolean sendMailResGes = configAlerteMailDto.getAlerteRespGestionnaire().isModificationConventionEtudiant();
            boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isModificationConventionEtudiant();
            sendValidationMail(convention, utilisateur,TemplateMail.CODE_ETU_MODIF_CONVENTION,
                    sendMailEtudiant, sendMailGestionnaire, sendMailResGes, sendMailEnseignant);
        }
        if (UtilisateurHelper.isRole(utilisateur, Role.GES)) {
            ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
            boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isModificationConventionGestionnaire();
            boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isModificationConventionGestionnaire();
            boolean sendMailResGes = configAlerteMailDto.getAlerteRespGestionnaire().isModificationConventionGestionnaire();
            boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isModificationConventionGestionnaire();
            sendValidationMail(convention, utilisateur,TemplateMail.CODE_GES_MODIF_CONVENTION,
                    sendMailEtudiant, sendMailGestionnaire, sendMailResGes, sendMailEnseignant);
        }
        return convention;
    }

    @PatchMapping("/{id}")
    @Secure(fonctions = AppFonctionEnum.CONVENTION, droits = {DroitEnum.MODIFICATION})
    public Convention singleFieldUpdate(@PathVariable("id") int id, @Valid @RequestBody ConventionSingleFieldDto conventionSingleFieldDto) {
        Convention convention = conventionJpaRepository.findById(id);
        setSingleFieldData(convention, conventionSingleFieldDto);
        convention = conventionJpaRepository.saveAndFlush(convention);
        return convention;
    }

    @GetMapping("/{annee}/en-attente-validation-alerte")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public int countConventionEnAttente(@PathVariable("annee") String annee) {
        ContextDto contexteDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contexteDto.getUtilisateur();
        List<Convention> conventions;
        boolean isEnseignant = false;
        // Récupération des conventions en attente de validation, pédagogique pour les enseignants, administrative pour les gestionnaires
        if (UtilisateurHelper.isRole(utilisateur, Role.ENS)) {
            conventions = conventionJpaRepository.getConventionEnAttenteEnseignant(appConfigService.getAnneeUnivLibelle(annee), utilisateur.getLogin());
            isEnseignant = true;
        } else if (!UtilisateurHelper.isRole(utilisateur, Role.ADM) && (UtilisateurHelper.isRole(utilisateur, Role.RESP_GES) || UtilisateurHelper.isRole(utilisateur, Role.GES))) {
            conventions = conventionJpaRepository.getConventionEnAttenteGestionnaire(appConfigService.getAnneeUnivLibelle(annee), utilisateur.getLogin());
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

    @GetMapping("/validation-creation/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public Convention validationCreation(@PathVariable("id") int id) {
        Convention convention = conventionJpaRepository.findById(id);

        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        convention.setValidationCreation(true);
        convention = conventionJpaRepository.saveAndFlush(convention);

        ContextDto contexteDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contexteDto.getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
            boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isCreationConventionEtudiant();
            boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isCreationConventionEtudiant();
            boolean sendMailResGes = configAlerteMailDto.getAlerteRespGestionnaire().isCreationConventionEtudiant();
            boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isCreationConventionEtudiant();
            sendValidationMail(convention, utilisateur,TemplateMail.CODE_ETU_CREA_CONVENTION,
                    sendMailEtudiant, sendMailGestionnaire, sendMailResGes, sendMailEnseignant);
        }
        if (UtilisateurHelper.isRole(utilisateur, Role.GES)) {
            ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
            boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isCreationConventionGestionnaire();
            boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isCreationConventionGestionnaire();
            boolean sendMailResGes = configAlerteMailDto.getAlerteRespGestionnaire().isCreationConventionGestionnaire();
            boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isCreationConventionGestionnaire();
            sendValidationMail(convention, utilisateur,TemplateMail.CODE_GES_CREA_CONVENTION,
                    sendMailEtudiant, sendMailGestionnaire, sendMailResGes, sendMailEnseignant);
        }
        return convention;
    }

    @PostMapping("/validation-administrative")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.VALIDATION})
    public int validationAdministrativeMultiple(@RequestBody IdsListDto idsListDto) {
        ContextDto contextDto = ServiceContext.getServiceContext();
        if (idsListDto.getIds().size() == 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La liste est vide");
        }
        ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
        int count = 0;
        for (int id : idsListDto.getIds()) {
            Convention convention = conventionJpaRepository.findById(id);
            if (convention == null) {
                continue;
            }
            validationAdministrative(convention, configAlerteMailDto, contextDto.getUtilisateur(), true);
            validationAutoDonnees(convention, contextDto.getUtilisateur());
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
        switch (type) {
            case "validationPedagogique":
                validationPedagogique(convention, appConfigService.getConfigAlerteMail(), ServiceContext.getServiceContext().getUtilisateur(), true);
                break;
            case "verificationAdministrative":
                verificationAdministrative(convention, ServiceContext.getServiceContext().getUtilisateur(), true);
                break;
            case "validationConvention":
                validationAdministrative(convention, appConfigService.getConfigAlerteMail(), ServiceContext.getServiceContext().getUtilisateur(), true);
                break;
            default:
                throw new AppException(HttpStatus.BAD_REQUEST, "Type de validation inconnu");
        }
        validationAutoDonnees(convention, ServiceContext.getServiceContext().getUtilisateur());
        return convention;
    }

    @PatchMapping("/{id}/devalider/{type}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.VALIDATION})
    public Convention unvalidate(@PathVariable("id") int id, @PathVariable("type") String type) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        switch (type) {
            case "validationPedagogique":
                validationPedagogique(convention, appConfigService.getConfigAlerteMail(), ServiceContext.getServiceContext().getUtilisateur(), false);
                break;
            case "verificationAdministrative":
                verificationAdministrative(convention, ServiceContext.getServiceContext().getUtilisateur(), false);
                break;
            case "validationConvention":
                validationAdministrative(convention, appConfigService.getConfigAlerteMail(), ServiceContext.getServiceContext().getUtilisateur(), false);
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
    public ResponseEntity<byte[]> getConventionPDF(@PathVariable("id") int id) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        ByteArrayOutputStream ou = new ByteArrayOutputStream();
        impressionService.generateConventionAvenantPDF(convention, ou, false);

        byte[] pdf = ou.toByteArray();
        return ResponseEntity.ok().body(pdf);
    }

    @GetMapping("/{id}/pdf-avenant")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> getAvenantPDF(@PathVariable("id") int id) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        ByteArrayOutputStream ou = new ByteArrayOutputStream();
        impressionService.generateConventionAvenantPDF(convention, ou, true);

        byte[] pdf = ou.toByteArray();
        return ResponseEntity.ok().body(pdf);
    }

    private void canViewEditConvention(Convention convention, Utilisateur utilisateur) {
        if (!UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                if (convention.getEtudiant() == null || !convention.getEtudiant().getIdentEtudiant().equalsIgnoreCase(utilisateur.getLogin())) {
                    throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
                }
            } else if (UtilisateurHelper.isRole(utilisateur, Role.ENS)) {
                if (convention.getEnseignant() == null || !convention.getEnseignant().getUidEnseignant().equalsIgnoreCase(utilisateur.getLogin())) {
                    throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
                }
            } else { // cas gestionnaire, responsable gestionnaire et profil non défini
                if (convention.getCentreGestion() == null || convention.getCentreGestion().getPersonnels() == null || convention.getCentreGestion().getPersonnels().stream().noneMatch(p -> p.getUidPersonnel().equalsIgnoreCase(utilisateur.getLogin()))) {
                    throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
                }
            }
        }
    }

    private boolean isConventionModifiable(Convention convention, Utilisateur utilisateur) {
        if (!UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                return !convention.isValidationCreation();
            } else if (UtilisateurHelper.isRole(utilisateur, Role.ENS)) {
                return false;
            } else { // cas gestionnaire, responsable gestionnaire et profil non défini
                return convention.getValidationConvention() == null || !convention.getValidationConvention();
            }
        }
        return true;
    }

    private void setConventionData(Convention convention, ConventionFormDto conventionFormDto) {
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        TypeConvention typeConvention = typeConventionJpaRepository.findById(conventionFormDto.getIdTypeConvention());
        if (typeConvention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Type de convention non trouvé");
        }
        LangueConvention langueConvention = langueConventionJpaRepository.findByCode(conventionFormDto.getCodeLangueConvention());
        if (langueConvention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Langue de convention non trouvée");
        }
        EtudiantRef etudiantRef = apogeeService.getInfoApogee(conventionFormDto.getNumEtudiant(), appConfigService.getAnneeUniv());
        if (etudiantRef == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Étudiant non trouvé");
        }
        Etape etape = etapeJpaRepository.findById(conventionFormDto.getCodeEtape(), conventionFormDto.getCodeVerionEtape(), appConfigService.getConfigGenerale().getCodeUniversite());
        if (etape == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Étape non trouvée");
        }
        Ufr ufr = ufrJpaRepository.findById(conventionFormDto.getCodeComposante(), appConfigService.getConfigGenerale().getCodeUniversite());
        if (ufr == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "UFR non trouvée");
        }
        CentreGestion centreGestion = null;
        // Recherche du centre de gestion par codeEtape/versionEtape
        CritereGestion critereGestion = critereGestionJpaRepository.findEtapeById(conventionFormDto.getCodeEtape(), conventionFormDto.getCodeVerionEtape());
        // Si non trouvé, recherche par code composante et version = ""
        if (critereGestion == null) {
            critereGestion = critereGestionJpaRepository.findEtapeById(conventionFormDto.getCodeComposante(), "");
        }
        // Si non trouvé on vérifie l'autorisation de création de convention non liée à un centre
        if (critereGestion == null) {
            // Erreur si on n'autorise pas la création de convention non rattaché à un centre de gestion
            if (!appConfigService.getConfigGenerale().isAutoriserConventionsOrphelines()) {
                throw new AppException(HttpStatus.NOT_FOUND, "Centre de gestion non trouvé");
            }
            // Sinon on prend le centre de type établissement
            centreGestion = centreGestionJpaRepository.getCentreEtablissement();
        } else {
            centreGestion = critereGestion.getCentreGestion();
        }
        // Erreur si le centre est null
        if (centreGestion == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Centre de gestion non trouvé");
        }

        canViewEditConvention(convention, ServiceContext.getServiceContext().getUtilisateur());
        if (!isConventionModifiable(convention, ServiceContext.getServiceContext().getUtilisateur())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La convention n'est plus modifiable");
        }

        Etudiant etudiant = etudiantJpaRepository.findByNumEtudiant(conventionFormDto.getNumEtudiant());
        if (etudiant == null) {
            etudiant = new Etudiant();
            etudiant.setIdentEtudiant(conventionFormDto.getEtudiantLogin());
            etudiant.setNumEtudiant(conventionFormDto.getNumEtudiant());
            etudiant.setNom(etudiantRef.getNompatro());
            etudiant.setPrenom(etudiantRef.getPrenom());
            etudiant.setMail(etudiantRef.getMail());
            etudiant.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
            etudiant.setCodeSexe(etudiantRef.getCodeSexe());
            etudiant.setDateNais(etudiantRef.getDateNais());
            etudiant = etudiantJpaRepository.saveAndFlush(etudiant);
        }
        convention.setEtudiant(etudiant);
        convention.setAdresseEtudiant(conventionFormDto.getAdresseEtudiant());
        convention.setCodePostalEtudiant(conventionFormDto.getCodePostalEtudiant());
        convention.setVilleEtudiant(conventionFormDto.getVilleEtudiant());
        convention.setPaysEtudiant(convention.getPaysEtudiant());
        convention.setTelEtudiant(conventionFormDto.getTelEtudiant());
        convention.setTelPortableEtudiant(conventionFormDto.getTelPortableEtudiant());
        convention.setCourrielPersoEtudiant(conventionFormDto.getCourrielPersoEtudiant());
        convention.setTypeConvention(typeConvention);
        convention.setLangueConvention(langueConvention);
        convention.setUfr(ufr);
        convention.setEtape(etape);
        convention.setCentreGestion(centreGestion);
        convention.setAnnee(conventionFormDto.getAnnee() + "/" + (Integer.parseInt(conventionFormDto.getAnnee()) + 1));
        convention.setCodeElp(conventionFormDto.getCodeElp());
        convention.setLibelleELP(conventionFormDto.getLibelleELP());
        convention.setCreditECTS(conventionFormDto.getCreditECTS());
        // mis à TRUE les validations non prises en compte dans le centre de gestion
        convention.setValidationConvention(!centreGestion.getValidationConvention());
        convention.setValidationPedagogique(!centreGestion.getValidationPedagogique());
        convention.setVerificationAdministrative(!centreGestion.getVerificationAdministrative());
    }

    private void setSingleFieldData(Convention convention, ConventionSingleFieldDto conventionSingleFieldDto) {
        canViewEditConvention(convention, ServiceContext.getServiceContext().getUtilisateur());
        if (!isConventionModifiable(convention, ServiceContext.getServiceContext().getUtilisateur())) {
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
        if (Objects.equals(conventionSingleFieldDto.getField(), "dateDebutInterruption")){
            Instant instant = Instant.parse((String) conventionSingleFieldDto.getValue()) ;
            convention.setDateDebutInterruption(java.util.Date.from(instant));
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "dateFinInterruption")){
            Instant instant = Instant.parse((String) conventionSingleFieldDto.getValue()) ;
            convention.setDateFinInterruption(java.util.Date.from(instant));
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "horairesReguliers")){
            convention.setHorairesReguliers((Boolean) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "nbHeuresHebdo")){
            convention.setNbHeuresHebdo((String) conventionSingleFieldDto.getValue());
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "quotiteTravail")){
            convention.setQuotiteTravail((Integer) conventionSingleFieldDto.getValue());
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
            Structure structure = structureJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (structure == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Structure non trouvé");
            }
            convention.setStructure(structure);
            //Cascade structure change to relevant fields
            convention.setService(null);
            convention.setContact(null);
            convention.setSignataire(null);
        }
        if (Objects.equals(conventionSingleFieldDto.getField(), "idService")){
            Service service = serviceJpaRepository.findById((int) conventionSingleFieldDto.getValue());
            if (service == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Service non trouvé");
            }
            convention.setService(service);
            //Cascade service change to relevant fields
            convention.setContact(null);
            convention.setSignataire(null);
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

    }

    private void validationPedagogique(Convention convention, ConfigAlerteMailDto configAlerteMailDto, Utilisateur utilisateurContext, boolean valider) {
        boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isValidationPedagogiqueConvention();
        boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isValidationPedagogiqueConvention();
        boolean sendMailResGes = configAlerteMailDto.getAlerteRespGestionnaire().isValidationPedagogiqueConvention();
        boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isValidationPedagogiqueConvention();

        HistoriqueValidation historique = new HistoriqueValidation();
        historique.setValeurAvant(convention.getValidationPedagogique());
        historique.setLogin(utilisateurContext.getLogin());
        historique.setType("validationPedagogique");
        historique.setConvention(convention);

        convention.setValidationPedagogique(valider);
        if (valider) {
            convention.setLoginValidation(utilisateurContext.getLogin());
        }
        setValeurNomenclature(convention);
        convention = conventionJpaRepository.saveAndFlush(convention);

        historique.setValeurApres(valider);
        historiqueValidationJpaRepository.saveAndFlush(historique);

        sendValidationMail(convention, utilisateurContext, valider ? TemplateMail.CODE_CONVENTION_VALID_PEDAGOGIQUE : TemplateMail.CODE_CONVENTION_DEVALID_PEDAGOGIQUE, sendMailEtudiant, sendMailGestionnaire, sendMailResGes, sendMailEnseignant);
    }

    private void verificationAdministrative(Convention convention, Utilisateur utilisateurContext, boolean valider) {
        HistoriqueValidation historique = new HistoriqueValidation();
        historique.setValeurAvant(convention.getVerificationAdministrative());
        historique.setLogin(utilisateurContext.getLogin());
        historique.setType("verificationAdministrative");
        historique.setConvention(convention);

        convention.setVerificationAdministrative(valider);
        setValeurNomenclature(convention);
        convention = conventionJpaRepository.saveAndFlush(convention);

        historique.setValeurApres(valider);
        historiqueValidationJpaRepository.saveAndFlush(historique);

        mailerService.sendAlerteValidation(convention.getEtudiant().getMail(), convention, utilisateurContext, valider ? TemplateMail.CODE_CONVENTION_VERIF_ADMINISTRATIVE : TemplateMail.CODE_CONVENTION_DEVERIF_ADMINISTRATIVE);
    }

    private void validationAdministrative(Convention convention, ConfigAlerteMailDto configAlerteMailDto, Utilisateur utilisateurContext, boolean valider) {
        boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isValidationAdministrativeConvention();
        boolean sendMailGestionnaire = configAlerteMailDto.getAlerteGestionnaire().isValidationAdministrativeConvention();
        boolean sendMailResGes = configAlerteMailDto.getAlerteRespGestionnaire().isValidationAdministrativeConvention();
        boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isValidationAdministrativeConvention();

        HistoriqueValidation historique = new HistoriqueValidation();
        historique.setValeurAvant(convention.getValidationConvention());
        historique.setLogin(utilisateurContext.getLogin());
        historique.setType("validationConvention");
        historique.setConvention(convention);

        convention.setValidationConvention(valider);
        if (valider) {
            convention.setLoginValidation(utilisateurContext.getLogin());
        }
        convention = conventionJpaRepository.saveAndFlush(convention);

        historique.setValeurApres(valider);
        historiqueValidationJpaRepository.saveAndFlush(historique);

        sendValidationMail(convention, utilisateurContext, valider ? TemplateMail.CODE_CONVENTION_VALID_ADMINISTRATIVE : TemplateMail.CODE_CONVENTION_DEVALID_ADMINISTRATIVE, sendMailEtudiant, sendMailGestionnaire, sendMailResGes, sendMailEnseignant);
    }

    private void validationAutoDonnees(Convention convention, Utilisateur utilisateur) {
        // Validation automatique de l'établissement d'accueil, le service d'accueil et du tuteur de stage à la validation de la convention
        if (
                convention.getValidationPedagogique() != null && convention.getValidationPedagogique()
                && convention.getVerificationAdministrative() != null && convention.getValidationPedagogique()
                && convention.getValidationConvention() != null && convention.getValidationConvention()
        ) {
            Structure structure = convention.getStructure();
            if (structure != null) {
                structure.setEstValidee(true);
                structure.setDateValidation(new Date());
                structure.setLoginValidation(utilisateur.getLogin());
                structure.setInfosAJour(new Date());
                structure.setLoginInfosAJour(utilisateur.getLogin());
                structureJpaRepository.save(structure);
            }
            Service service = convention.getService();
            if (service != null) {
                service.setInfosAJour(new Date());
                service.setLoginInfosAJour(utilisateur.getLogin());
                serviceJpaRepository.save(service);
            }
            Contact tuteurPro = convention.getContact();
            if (tuteurPro != null) {
                tuteurPro.setInfosAJour(new Date());
                tuteurPro.setLoginInfosAJour(utilisateur.getLogin());
                contactJpaRepository.save(tuteurPro);
            }
        }
    }

    private void setValeurNomenclature(Convention convention) {
        ConventionNomenclature conventionNomenclature = convention.getNomenclature();
        if (conventionNomenclature == null) {
            conventionNomenclature = new ConventionNomenclature();
            conventionNomenclature.setConvention(convention);
        }
        conventionNomenclature.setLangueConvention(convention.getLangueConvention().getLibelle());
        conventionNomenclature.setDevise(convention.getDevise() != null ? convention.getDevise().getLibelle() : null);
        conventionNomenclature.setModeValidationStage(convention.getModeValidationStage() != null ? convention.getModeValidationStage().getLibelle() : null);
        conventionNomenclature.setModeVersGratification(convention.getModeVersGratification() != null ? convention.getModeVersGratification().getLibelle() : null);
        conventionNomenclature.setNatureTravail(convention.getNatureTravail() != null ? convention.getNatureTravail().getLibelle() : null);
        conventionNomenclature.setOrigineStage(convention.getOrigineStage() != null ? convention.getOrigineStage().getLibelle() : null);
        conventionNomenclature.setTempsTravail(convention.getTempsTravail() != null ? convention.getTempsTravail().getLibelle() : null);
        conventionNomenclature.setTheme(convention.getTheme() != null ? convention.getTheme().getLibelle() : null);
        conventionNomenclature.setTypeConvention(convention.getTypeConvention().getLibelle());
        conventionNomenclature.setUniteDureeExceptionnelle(convention.getUniteDureeExceptionnelle() != null ? convention.getUniteDureeExceptionnelle().getLibelle() : null);
        conventionNomenclature.setUniteDureeGratification(convention.getUniteDureeGratification() != null ? convention.getUniteDureeGratification().getLibelle() : null);
        conventionNomenclature.setUniteGratification(convention.getUniteGratification() != null ? convention.getUniteGratification().getLibelle() : null);
        convention.setNomenclature(conventionNomenclature);
    }

    public void sendValidationMail(Convention convention, Utilisateur utilisateurContext, String templateMailCode, boolean sendMailEtudiant, boolean sendMailGestionnaire, boolean sendMailResGes, boolean sendMailEnseignant) {
        // Récupération du personnel du centre de gestion de la convention avec alertMail=1
        List<PersonnelCentreGestion> personnels = convention.getCentreGestion().getPersonnels();
        personnels = personnels.stream().filter(PersonnelCentreGestion::getAlertesMail).collect(Collectors.toList());

        // Récupération de la fiche utilisateur des personnels
        List<Utilisateur> utilisateurPersonnels = utilisateurJpaRepository.findByLogins(personnels.stream().map(PersonnelCentreGestion::getUidPersonnel).collect(Collectors.toList()));

        // Envoi du mail de validation administrative
        if (sendMailEtudiant) mailerService.sendAlerteValidation(convention.getEtudiant().getMail(), convention, utilisateurContext, templateMailCode);
        if (sendMailGestionnaire) {
            // Parmi le personnel avec alertMail=1, on ne garde que ceux qui n'ont pas le rôle RESP_GES pour éviter l'envoi en double du mail à la même personne
            for (PersonnelCentreGestion personnel : personnels) {
                Utilisateur utilisateur = utilisateurPersonnels.stream().filter(u -> u.getLogin().equals(personnel.getUidPersonnel())).findAny().orElse(null);
                if (utilisateur == null || !UtilisateurHelper.isRole(utilisateur, Role.RESP_GES)) {
                    mailerService.sendAlerteValidation(personnel.getMail(), convention, utilisateurContext, templateMailCode);
                }
            }
        }
        if (sendMailResGes) {
            // Parmi le personnel avec alertMail=1, on ne garde ceux qui ont le rôle RESP_GES pour éviter l'envoi en double du mail à la même personne
            mailerService.sendAlerteValidation(convention.getEtudiant().getMail(), convention, utilisateurContext, templateMailCode);
            for (PersonnelCentreGestion personnel : personnels) {
                Utilisateur utilisateur = utilisateurPersonnels.stream().filter(u -> u.getLogin().equals(personnel.getUidPersonnel())).findAny().orElse(null);
                if (utilisateur != null && UtilisateurHelper.isRole(utilisateur, Role.RESP_GES)) {
                    mailerService.sendAlerteValidation(personnel.getMail(), convention, utilisateurContext, templateMailCode);
                }
            }
        }
        if (sendMailEnseignant) mailerService.sendAlerteValidation(convention.getEnseignant().getMail(), convention, utilisateurContext, templateMailCode);
    }
}
