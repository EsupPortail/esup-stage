package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ContextDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.AvenantDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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
    AvenantJpaRepository avenantJpaRepository;

    @Autowired
    MailerService mailerService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Avenant> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Avenant> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(avenantRepository.count(filters));
        paginatedResponse.setData(avenantRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.LECTURE})
    public Avenant getById(@PathVariable("id") int id) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
        }
        return avenant;
    }

    @GetMapping("/getByConvention/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.LECTURE})
    public List<Avenant> getByConvention(@PathVariable("id") int id) {
        List<Avenant> avenant = avenantJpaRepository.findByConvention(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
        }
        return avenant;
    }

    @GetMapping("/validate/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.VALIDATION})
    public Avenant validate(@PathVariable("id") int id) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
        }
        avenant.setValidationAvenant(true);
        return avenantJpaRepository.saveAndFlush(avenant);
    }

    @GetMapping("/validate/cancel/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.VALIDATION})
    public Avenant cancelValidation(@PathVariable("id") int id) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
        }
        avenant.setValidationAvenant(false);
        return avenantJpaRepository.saveAndFlush(avenant);
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.CREATION})
    public Avenant create(@Valid @RequestBody AvenantDto avenantDto) {
        Avenant avenant = new Avenant();
        setAvenantData(avenant, avenantDto);
        avenant = avenantJpaRepository.saveAndFlush(avenant);

        ContextDto contexteDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contexteDto.getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            mailerService.sendAlerteCreationAvenantParEtudiant(avenant.getConvention().getCentreGestion().getMail(), avenant, utilisateur);
        }
        return avenant;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.MODIFICATION})
    public Avenant update(@PathVariable("id") int id, @Valid @RequestBody AvenantDto avenantDto) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvé");
        }
        setAvenantData(avenant, avenantDto);
        return avenantJpaRepository.saveAndFlush(avenant);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvé");
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
}