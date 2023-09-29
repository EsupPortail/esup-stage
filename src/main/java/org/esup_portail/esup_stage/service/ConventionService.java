package org.esup_portail.esup_stage.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.esup_portail.esup_stage.docaposte.DocaposteClient;
import org.esup_portail.esup_stage.dto.ConventionFormDto;
import org.esup_portail.esup_stage.dto.ResponseDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConventionService {

    private static final Logger logger	= LogManager.getLogger(ConventionService.class);

    @Autowired
    EtudiantJpaRepository etudiantJpaRepository;

    @Autowired
    EtudiantRepository etudiantRepository;

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
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    PaysJpaRepository paysJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    ApogeeService apogeeService;

    @Autowired
    DocaposteClient docaposteClient;

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
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(conventionFormDto.getEtudiantLogin())) {
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
        Etape etape = etapeJpaRepository.findById(conventionFormDto.getCodeEtape(), conventionFormDto.getCodeVersionEtape(), appConfigService.getConfigGenerale().getCodeUniversite());
        if (etape == null) {
            EtapeId etapeId = new EtapeId();
            etapeId.setCode(conventionFormDto.getCodeEtape());
            etapeId.setCodeVersionEtape(conventionFormDto.getCodeVersionEtape());
            etapeId.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
            etape = new Etape();
            etape.setId(etapeId);
            etape.setLibelle(conventionFormDto.getLibelleEtape());
            etape = etapeJpaRepository.saveAndFlush(etape);
        }
        Ufr ufr = ufrJpaRepository.findById(conventionFormDto.getCodeComposante(), appConfigService.getConfigGenerale().getCodeUniversite());
        if (ufr == null) {
            UfrId ufrId = new UfrId();
            ufrId.setCode(conventionFormDto.getCodeComposante());
            ufrId.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
            ufr = new Ufr();
            ufr.setId(ufrId);
            ufr.setLibelle(conventionFormDto.getLibelleComposante());
            ufr = ufrJpaRepository.saveAndFlush(ufr);
        }
        CentreGestion centreGestionEtab = centreGestionJpaRepository.getCentreEtablissement();
        // Erreur si le centre de type etablissement est null
        if (centreGestionEtab == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Centre de gestion de type établissement non trouvé");
        }
        CentreGestion centreGestion = null;
        // Recherche du centre de gestion par codeEtape/versionEtape
        CritereGestion critereGestion = critereGestionJpaRepository.findEtapeById(conventionFormDto.getCodeEtape(), conventionFormDto.getCodeVersionEtape());
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

        Etudiant etudiant = etudiantRepository.findByNumEtudiant(conventionFormDto.getNumEtudiant());
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

        // Ajout du pays de la convention à France si non renseigné
        if (convention.getPaysConvention() == null) {
            convention.setPaysConvention(paysJpaRepository.findByIso2("FR"));
        }

        convention.setEtudiant(etudiant);
        convention.setAdresseEtudiant(conventionFormDto.getAdresseEtudiant());
        convention.setCodePostalEtudiant(conventionFormDto.getCodePostalEtudiant());
        convention.setVilleEtudiant(conventionFormDto.getVilleEtudiant());
        convention.setPaysEtudiant(conventionFormDto.getPaysEtudiant());
        convention.setTelEtudiant(conventionFormDto.getTelEtudiant());
        convention.setTelPortableEtudiant(conventionFormDto.getTelPortableEtudiant());
        convention.setCourrielPersoEtudiant(conventionFormDto.getCourrielPersoEtudiant());
        convention.setLibelleCPAM(conventionFormDto.getLibelleCPAM());
        convention.setRegionCPAM(conventionFormDto.getRegionCPAM());
        convention.setAdresseCPAM(conventionFormDto.getAdresseCPAM());
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
        convention.setVolumeHoraireFormation(conventionFormDto.getVolumeHoraireFormation());

        canViewEditConvention(convention, ServiceContext.getUtilisateur());
        if (!isConventionModifiable(convention, ServiceContext.getUtilisateur())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La convention n'est plus modifiable");
        }
    }

    public void canViewEditConvention(Convention convention, Utilisateur utilisateur) {
        if (!UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                if (convention.getEtudiant() == null || !convention.getEtudiant().getIdentEtudiant().equalsIgnoreCase(utilisateur.getUid())) {
                    throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
                }
            } else if (UtilisateurHelper.isRole(utilisateur, Role.ENS)) {
                if (convention.getEnseignant() == null || !convention.getEnseignant().getUidEnseignant().equalsIgnoreCase(utilisateur.getUid())) {
                    throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
                }
            } else { // cas gestionnaire, responsable gestionnaire et profil non défini
                if (convention.getCentreGestion() == null || convention.getCentreGestion().getPersonnels() == null || convention.getCentreGestion().getPersonnels().stream().noneMatch(p -> p.getUidPersonnel().equalsIgnoreCase(utilisateur.getUid()))) {
                    throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
                }
            }
        }
    }

    public boolean isConventionModifiable(Convention convention, Utilisateur utilisateur) {
        if (!UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                boolean modifiable = true;
                CentreGestion centreGestion = convention.getCentreGestion();
                if (centreGestion.getValidationConvention() != null && centreGestion.getValidationConvention() && convention.getValidationConvention() != null && convention.getValidationConvention()) {
                    modifiable = false;
                }
                if (centreGestion.getVerificationAdministrative() != null && centreGestion.getVerificationAdministrative() && convention.getVerificationAdministrative() != null && convention.getVerificationAdministrative()) {
                    modifiable = false;
                }
                if (centreGestion.getValidationPedagogique() != null && centreGestion.getValidationPedagogique() && convention.getValidationPedagogique() != null && convention.getValidationPedagogique()) {
                    modifiable = false;
                }
                return modifiable;
            } else if (UtilisateurHelper.isRole(utilisateur, Role.ENS)) {
                return false;
            } else { // cas gestionnaire, responsable gestionnaire et profil non défini
                return convention.getValidationConvention() == null || !convention.getValidationConvention();
            }
        }
        return true;
    }

    public ResponseDto controleEmailTelephone(Convention convention) {
        // Contrôle de la présence soit du mail, soit du numéro de téléphone pour les profils liés à la signature électronique
        // Si les 2 manquantes, on affiche une erreur
        // Si une donnée est manquante on affiche un warning avec possibilité de continuer
        ResponseDto response = new ResponseDto();
        String keyAdr = "adresse mail";
        String keyTel = "numéro téléphone";
        Map<String, Map<String, String>> data = new HashMap<>();
        data.put("étudiant", new HashMap<>() {{
            put(keyAdr, convention.getEtudiant().getMail());
            put(keyTel, convention.getTelPortableEtudiant());
        }});
        data.put("enseignant référent", new HashMap<>() {{
            put(keyAdr, convention.getEnseignant().getMail());
            put(keyTel, convention.getEnseignant().getTel());
        }});
        data.put("tuteur pédagogique", new HashMap<>() {{
            put(keyAdr, convention.getContact().getMail());
            put(keyTel, convention.getContact().getTel());
        }});
        data.put("signataire", new HashMap<>() {{
            put(keyAdr, convention.getSignataire().getMail());
            put(keyTel, convention.getSignataire().getTel());
        }});
        data.put("directeur du département", new HashMap<>() {{
            put(keyAdr, convention.getCentreGestion().getMail());
            put(keyTel, convention.getCentreGestion().getTelephone());
        }});

        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            Map<String, String> values = entry.getValue();
            List<String> donneesManquantes = new ArrayList<>();
            if (values.get(keyAdr) == null || values.get(keyAdr).isEmpty()) {
                donneesManquantes.add(keyAdr);
            }
            if (values.get(keyTel) == null || values.get(keyTel).isEmpty()) {
                donneesManquantes.add(keyTel);
            }
            if (donneesManquantes.size() == 2) {
                response.getError().add(entry.getKey() + " : " + String.join(", ", donneesManquantes));
            } else if (donneesManquantes.size() == 1) {
                response.getWarning().add(entry.getKey() + " : " + String.join(", ", donneesManquantes));
            }
        }
        return response;
    }

    public void updateSignatureElectroniqueHistorique() {
        List<Convention> conventions = conventionJpaRepository.getSignatureInfoToUpdate();
        for (Convention convention : conventions) {
            updateSignatureElectroniqueHistorique(convention);
        }
    }

    public void updateSignatureElectroniqueHistorique(Convention convention) {
        try {
            List<Historique> historiques = docaposteClient.getHistorique(convention.getDocumentId(), convention.getCentreGestion().getSignataires());
            for (Historique historique : historiques) {
                switch (historique.getTypeSignataire()) {
                    case etudiant:
                        convention.setDateSignatureEtudiant(historique.getDateSignature());
                        convention.setDateDepotEtudiant(historique.getDateDepot());
                        break;
                    case enseignant:
                        convention.setDateSignatureEnseignant(historique.getDateSignature());
                        convention.setDateDepotEnseignant(historique.getDateDepot());
                        break;
                    case tuteur:
                        convention.setDateSignatureTuteur(historique.getDateSignature());
                        convention.setDateDepotTuteur(historique.getDateDepot());
                        break;
                    case signataire:
                        convention.setDateSignatureSignataire(historique.getDateSignature());
                        convention.setDateDepotSignataire(historique.getDateDepot());
                        break;
                    case viseur:
                        convention.setDateSignatureViseur(historique.getDateSignature());
                        convention.setDateDepotViseur(historique.getDateDepot());
                        break;
                    default:
                        break;
                }
            }
            conventionJpaRepository.save(convention);
        } catch (Exception e) {
            logger.error(e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur de la récupération de l'historique");
        }
    }

    public String parseNumTel(String numTel) {
        if (!Strings.isEmpty(numTel)) {
            try {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(numTel, "FR");
                if (phoneUtil.isValidNumber(phoneNumber)) {
                    return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
                }
            } catch (NumberParseException e) {
                logger.error("Numéro de téléphone non valide : " + numTel);
            }
        }
        return null;
    }
}
