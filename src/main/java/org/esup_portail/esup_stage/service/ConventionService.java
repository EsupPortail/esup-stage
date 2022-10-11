package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.dto.ConventionFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ConventionService {

    @Autowired
    EtudiantJpaRepository etudiantJpaRepository;

    @Autowired
    EtapeJpaRepository etapeJpaRepository;

    @Autowired
    UfrJpaRepository ufrJpaRepository;

    @Autowired
    CritereGestionJpaRepository critereGestionJpaRepository;

    @Autowired
    StructureJpaRepository structureJpaRepository;

    @Autowired
    ServiceJpaRepository serviceJpaRepository;

    @Autowired
    ContactJpaRepository contactJpaRepository;

    @Autowired
    TypeConventionJpaRepository typeConventionJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    LangueConventionJpaRepository langueConventionJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    ApogeeService apogeeService;

    public void validationAutoDonnees(Convention convention, Utilisateur utilisateur) {
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
            org.esup_portail.esup_stage.model.Service service = convention.getService();
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

    public void setConventionData(Convention convention, ConventionFormDto conventionFormDto) {
        // Pour les étudiants on vérifie que c'est une de ses conventions
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getLogin().equals(conventionFormDto.getEtudiantLogin())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
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
        CentreGestion centreGestionEtab = centreGestionJpaRepository.getCentreEtablissement();
        // Erreur si le centre de type etablissement est null
        if (centreGestionEtab == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Centre de gestion de type établissement non trouvé");
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
            centreGestion = centreGestionEtab;
        } else {
            centreGestion = critereGestion.getCentreGestion();
        }
        // Erreur si le centre est null
        if (centreGestion == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Centre de gestion non trouvé");
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
        }
        etudiant.setCodeSexe(etudiantRef.getCodeSexe());
        etudiant.setDateNais(etudiantRef.getDateNais());
        etudiant = etudiantJpaRepository.saveAndFlush(etudiant);

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
        convention.setNomEtabRef(centreGestionEtab.getNomCentre());
        convention.setAdresseEtabRef(centreGestionEtab.getAdresseComplete());

        canViewEditConvention(convention, ServiceContext.getUtilisateur());
        if (!isConventionModifiable(convention, ServiceContext.getUtilisateur())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La convention n'est plus modifiable");
        }
    }

    public void canViewEditConvention(Convention convention, Utilisateur utilisateur) {
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

    public boolean isConventionModifiable(Convention convention, Utilisateur utilisateur) {
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
}
