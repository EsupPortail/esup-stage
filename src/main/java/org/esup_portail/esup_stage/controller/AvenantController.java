package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.docaposte.DocaposteClient;
import org.esup_portail.esup_stage.dto.AvenantDto;
import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.IdsListDto;
import org.esup_portail.esup_stage.dto.ResponseDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConventionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@ApiController
@RequestMapping("/avenant")
public class AvenantController {

    @Autowired
    ConventionJpaRepository conventionJpaRepository;
    @Autowired
    ServiceJpaRepository serviceJpaRepository;
    @Autowired
    ContactJpaRepository contactJpaRepository;
    @Autowired
    EnseignantJpaRepository enseignantJpaRepository;
    @Autowired
    UniteGratificationJpaRepository uniteGratificationJpaRepository;
    @Autowired
    UniteDureeJpaRepository uniteDureeJpaRepository;
    @Autowired
    ModeVersGratificationJpaRepository modeVersGratificationJpaRepository;
    @Autowired
    DeviseJpaRepository deviseJpaRepository;

    @Autowired
    AvenantRepository avenantRepository;

    @Autowired
    PeriodeInterruptionAvenantJpaRepository periodeInterruptionAvenantJpaRepository;

    @Autowired
    AvenantJpaRepository avenantJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    ConventionController conventionController;
    @Autowired
    ConventionService conventionService;
    @Autowired
    DocaposteClient docaposteClient;



    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.LECTURE})
    public Avenant getById(@PathVariable("id") int id) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
        }
        // Pour les étudiants, on vérifie que c'est bien un avenant d'une de ses covnentions
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(avenant.getConvention().getEtudiant().getIdentEtudiant())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
        }
        return avenant;
    }

    @GetMapping("/getByConvention/{id}")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.LECTURE})
    public List<Avenant> getByConvention(@PathVariable("id") int id) {
        // Pour les étudiants, on vérifie que c'est bien un avenant d'une de ses convention
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            Convention convention = conventionJpaRepository.getById(id);
            if (!utilisateur.getUid().equals(convention.getEtudiant().getIdentEtudiant())) {
                throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
            }
        }
        List<Avenant> avenant = avenantJpaRepository.findByConvention(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
        }
        return avenant;
    }

    @GetMapping("/validate/{id}")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.VALIDATION})
    public Avenant validate(@PathVariable("id") int id) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
        }
        avenant.setValidationAvenant(true);
        avenant.setDateValidation(new Date());
        return avenantJpaRepository.saveAndFlush(avenant);
    }

    @GetMapping("/validate/cancel/{id}")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.VALIDATION})
    public Avenant cancelValidation(@PathVariable("id") int id) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
        }
        if (avenant.getDocumentId() != null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Avenant déjà envoyé pour signature");
        }
        avenant.setValidationAvenant(false);
        avenant.setDateValidation(null);
        return avenantJpaRepository.saveAndFlush(avenant);
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.CREATION})
    public Avenant create(@Valid @RequestBody AvenantDto avenantDto) {
        Avenant avenant = new Avenant();
        setAvenantData(avenant, avenantDto);
        avenant = avenantJpaRepository.saveAndFlush(avenant);

        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
            boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isCreationAvenantEtudiant();
            boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isCreationAvenantEtudiant();
            conventionController.sendValidationMail(avenant.getConvention(), utilisateur,TemplateMail.CODE_ETU_CREA_AVENANT, sendMailEtudiant, sendMailEnseignant);
        }
        if (UtilisateurHelper.isRole(utilisateur, Role.GES)) {
            ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
            boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isCreationAvenantGestionnaire();
            boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isCreationAvenantGestionnaire();
            conventionController.sendValidationMail(avenant.getConvention(), utilisateur,TemplateMail.CODE_GES_CREA_AVENANT, sendMailEtudiant, sendMailEnseignant);
        }
        return avenant;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.MODIFICATION})
    public Avenant update(@PathVariable("id") int id, @Valid @RequestBody AvenantDto avenantDto) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvé");
        }
        setAvenantData(avenant, avenantDto);
        avenant = avenantJpaRepository.saveAndFlush(avenant);

        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
            boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isModificationAvenantEtudiant();
            boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isModificationAvenantEtudiant();
            conventionController.sendValidationMail(avenant.getConvention(), utilisateur,TemplateMail.CODE_ETU_MODIF_AVENANT, sendMailEtudiant, sendMailEnseignant);
        }
        if (UtilisateurHelper.isRole(utilisateur, Role.GES)) {
            ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
            boolean sendMailEtudiant = configAlerteMailDto.getAlerteEtudiant().isModificationAvenantGestionnaire();
            boolean sendMailEnseignant = configAlerteMailDto.getAlerteEnseignant().isModificationAvenantGestionnaire();
            conventionController.sendValidationMail(avenant.getConvention(), utilisateur,TemplateMail.CODE_GES_MODIF_AVENANT, sendMailEtudiant, sendMailEnseignant);
        }
        return avenant;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvé");
        }
        List<PeriodeInterruptionAvenant> periodeInterruptionAvenants = periodeInterruptionAvenantJpaRepository.findByAvenant(id);
        for(PeriodeInterruptionAvenant periodeInterruptionAvenant : periodeInterruptionAvenants){
            periodeInterruptionAvenantJpaRepository.delete(periodeInterruptionAvenant);
            periodeInterruptionAvenantJpaRepository.flush();
        }
        avenantJpaRepository.delete(avenant);
        avenantJpaRepository.flush();
        return true;
    }

    private void setAvenantData(Avenant avenant, AvenantDto avenantDto) {

        Convention convention = conventionJpaRepository.findById(avenantDto.getIdConvention());
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvé");
        }
        // Pour les étudiants, on vérifie que c'est bien un avenant d'une de ses convention
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(convention.getEtudiant().getIdentEtudiant())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvé");
        }

        if (avenantDto.getIdService() != 0) {
            Service service = serviceJpaRepository.findById(avenantDto.getIdService());
            if (service == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Service non trouvé");
            }
            avenant.setService(service);
        }
        if (avenantDto.getIdContact() != 0) {
            Contact contact = contactJpaRepository.findById(avenantDto.getIdContact());
            if (contact == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouvé");
            }
            avenant.setContact(contact);
        }
        if (avenantDto.getIdEnseignant() != 0) {
            Enseignant enseignant = enseignantJpaRepository.findById(avenantDto.getIdEnseignant());
            if (enseignant == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Enseignant non trouvé");
            }
            avenant.setEnseignant(enseignant);
        }
        if (avenantDto.getIdUniteGratification() != 0) {
            UniteGratification uniteGratification = uniteGratificationJpaRepository.findById(avenantDto.getIdUniteGratification());
            if (uniteGratification == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "UniteGratification non trouvé");
            }
            avenant.setUniteGratification(uniteGratification);
        }
        if (avenantDto.getIdUniteDuree() != 0) {
            UniteDuree uniteDuree = uniteDureeJpaRepository.findById(avenantDto.getIdUniteDuree());
            if (uniteDuree == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "UniteDuree non trouvé");
            }
            avenant.setUniteDuree(uniteDuree);
        }
        if (avenantDto.getIdModeVersGratification() != 0) {
            ModeVersGratification modeVersGratification = modeVersGratificationJpaRepository.findById(avenantDto.getIdModeVersGratification());
            if (modeVersGratification == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "ModeVersGratification non trouvé");
            }
            avenant.setModeVersGratification(modeVersGratification);
        }
        if (avenantDto.getIdDevise() != 0) {
            Devise devise = deviseJpaRepository.findById(avenantDto.getIdDevise());
            if (devise == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Devise non trouvé");
            }
            avenant.setDevise(devise);
        }

        avenant.setConvention(convention);
        avenant.setTitreAvenant(avenantDto.getTitreAvenant());
        avenant.setMotifAvenant(avenantDto.getMotifAvenant());
        avenant.setRupture(avenantDto.getRupture());
        avenant.setModificationPeriode(avenantDto.getModificationPeriode());
        avenant.setDateDebutStage(avenantDto.getDateDebutStage());
        avenant.setDateFinStage(avenantDto.getDateFinStage());
        avenant.setInterruptionStage(avenantDto.getInterruptionStage());
        avenant.setDateDebutInterruption(avenantDto.getDateDebutInterruption());
        avenant.setDateFinInterruption(avenantDto.getDateFinInterruption());
        avenant.setModificationLieu(avenantDto.getModificationLieu());
        avenant.setModificationSujet(avenantDto.getModificationSujet());
        avenant.setSujetStage(avenantDto.getSujetStage());
        avenant.setModificationEnseignant(avenantDto.getModificationEnseignant());
        avenant.setModificationSalarie(avenantDto.getModificationSalarie());
        avenant.setValidationAvenant(avenantDto.getValidationAvenant());
        avenant.setMontantGratification(avenantDto.getMontantGratification());
        avenant.setModificationMontantGratification(avenantDto.getModificationMontantGratification());
        avenant.setDateRupture(avenantDto.getDateRupture());
        avenant.setCommentaireRupture(avenantDto.getCommentaireRupture());
        avenant.setMonnaieGratification(avenantDto.getMonnaieGratification());
    }

    @PostMapping("/signature-electronique")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.VALIDATION})
    public int envoiSignatureElectroniqueMultiple(@RequestBody IdsListDto idsListDto) {
        if (!appConfigService.getConfigGenerale().isDocaposteEnabled()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La signature électronique n'est pas configurée");
        }
        if (idsListDto.getIds().size() == 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La liste est vide");
        }
        int count = 0;
        for (int id : idsListDto.getIds()) {
            Avenant avenant = avenantJpaRepository.findById(id);
            if (avenant == null || avenant.getConvention().getCentreGestion().getCircuitSignature() == null) {
                continue;
            }
            ResponseDto controles = conventionService.controleEmailTelephone(avenant.getConvention());
            if (controles.getError().size() > 0) {
                continue;
            }
            docaposteClient.upload(avenant.getConvention(),avenant);
            count++;
        }
        return count;
    }

    @PostMapping("/{id}/controle-signature-electronique")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.VALIDATION})
    public ResponseDto controleSignatureElectronique(@PathVariable("id") int id) {
        if (!appConfigService.getConfigGenerale().isDocaposteEnabled()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La signature électronique n'est pas configurée");
        }
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvé");
        }
        if (avenant.getConvention().getCentreGestion().getCircuitSignature() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Le centre de gestion " + avenant.getConvention().getCentreGestion().getNomCentre() + " n'a pas de circuit de signature");
        }
        return conventionService.controleEmailTelephone(avenant.getConvention());
    }
}