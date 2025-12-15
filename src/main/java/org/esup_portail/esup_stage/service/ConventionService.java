package org.esup_portail.esup_stage.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.esup_portail.esup_stage.dto.ConventionFormDto;
import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.dto.ResponseDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.enums.SignataireEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.service.Structure.StructureService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.esup_portail.esup_stage.service.apogee.model.InfosAdmEtu;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.esup_portail.esup_stage.service.signature.SignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConventionService {

    private static final Logger logger = LogManager.getLogger(ConventionService.class);

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

    @Lazy
    @Autowired
    SignatureService signatureService;

    @Autowired
    LdapService ldapService;

    @Autowired
    private StructureService structureService;

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
                structureService.save(null,structure);
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
            if (updateNomSignataireComposante(convention)) {
                conventionJpaRepository.save(convention);
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
        TypeConvention typeConvention = getTypeConvention(conventionFormDto.getIdTypeConvention());
        convention.setTypeConvention(typeConvention);
        LangueConvention langueConvention = getLangueConvention(conventionFormDto.getCodeLangueConvention());
        convention.setLangueConvention(langueConvention);
        EtudiantRef etudiantRef = apogeeService.getInfoApogee(conventionFormDto.getNumEtudiant(), appConfigService.getAnneeUniv());
        if (etudiantRef == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Étudiant non trouvé");
        }
        LdapSearchDto ldapSearchDto = new LdapSearchDto();
        ldapSearchDto.setCodEtu(conventionFormDto.getNumEtudiant());
        List<LdapUser> ldapEtudiant = ldapService.search("/etudiant", ldapSearchDto);
        if (ldapEtudiant == null || ldapEtudiant.size() == 0) {
            throw new AppException(HttpStatus.NOT_FOUND, "Étudiant non trouvé");
        }
        Etape etape = getEtape(conventionFormDto.getCodeEtape(), conventionFormDto.getCodeVersionEtape(), conventionFormDto.getLibelleEtape());
        convention.setEtape(etape);
        Ufr ufr = getUfr(conventionFormDto.getCodeComposante(), conventionFormDto.getLibelleComposante());
        convention.setUfr(ufr);
        CentreGestion centreGestionEtab = getCentreGestionEtab();
        CentreGestion centreGestion = getCentreGestion(centreGestionEtab, conventionFormDto.getCodeComposante(), conventionFormDto.getCodeEtape(), conventionFormDto.getCodeVersionEtape());
        convention.setCentreGestion(centreGestion);

        Etudiant etudiant = etudiantRepository.findByNumEtudiant(conventionFormDto.getNumEtudiant());
        if (etudiant == null) {
            etudiant = new Etudiant();
            etudiant.setIdentEtudiant(ldapEtudiant.getFirst().getUid());
            etudiant.setNumEtudiant(conventionFormDto.getNumEtudiant());
            etudiant.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
        }
        etudiant.setNom(String.join(" ", etudiantRef.getNompatro()));
        etudiant.setPrenom(String.join(" ", etudiantRef.getPrenom()));
        etudiant.setMail(etudiantRef.getMail());
        etudiant.setCodeSexe(etudiantRef.getCodeSexe());
        etudiant.setDateNais(etudiantRef.getDateNais());

        InfosAdmEtu infosAdmEtu = apogeeService.getInfosAdmEtudiant(etudiant.getNumEtudiant());
        if (infosAdmEtu != null) {
            etudiant.setPrenomEtatCivil(infosAdmEtu.getPrenomEtatCivil());
            etudiant.setSexEtatCivil(infosAdmEtu.getSexEtatCivil());
            etudiant.setPrenom2(infosAdmEtu.getPrenom2());
        }

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
        convention.setAnnee(conventionFormDto.getAnnee() + "/" + (Integer.parseInt(conventionFormDto.getAnnee()) + 1));
        convention.setCodeElp(conventionFormDto.getCodeElp());
        convention.setLibelleELP(conventionFormDto.getLibelleELP());
        convention.setCreditECTS(conventionFormDto.getCreditECTS());
        convention.setNomEtabRef(centreGestionEtab.getNomCentre());
        convention.setAdresseEtabRef(centreGestionEtab.getAdresseComplete());
        convention.setVolumeHoraireFormation(conventionFormDto.getVolumeHoraireFormation());
        convention.setProtectionSocialeOrganismeAccueil(conventionFormDto.getProtectionSocialeOrganismeAccueil());

        if (!isConventionModifiable(convention, ServiceContext.getUtilisateur())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La convention n'est plus modifiable");
        }
    }

    public TypeConvention getTypeConvention(int id) {
        TypeConvention typeConvention = typeConventionJpaRepository.findById(id);
        if (typeConvention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Type de convention non trouvé");
        }
        return typeConvention;
    }

    public LangueConvention getLangueConvention(String code) {
        LangueConvention langueConvention = langueConventionJpaRepository.findByCode(code);
        if (langueConvention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Langue de convention non trouvée");
        }
        return langueConvention;
    }

    public Etape getEtape(String codeEtape, String versionEtape, String libelleEtape) {
        Etape etape = etapeJpaRepository.findById(codeEtape, versionEtape, appConfigService.getConfigGenerale().getCodeUniversite());
        if (etape == null) {
            EtapeId etapeId = new EtapeId();
            etapeId.setCode(codeEtape);
            etapeId.setCodeVersionEtape(versionEtape);
            etapeId.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
            etape = new Etape();
            etape.setId(etapeId);
            etape.setLibelle(libelleEtape);
            etape = etapeJpaRepository.saveAndFlush(etape);
        }
        return etape;
    }

    public Ufr getUfr(String codeComposante, String libelleComposante) {
        Ufr ufr = ufrJpaRepository.findById(codeComposante, appConfigService.getConfigGenerale().getCodeUniversite());
        if (ufr == null) {
            UfrId ufrId = new UfrId();
            ufrId.setCode(codeComposante);
            ufrId.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
            ufr = new Ufr();
            ufr.setId(ufrId);
            ufr.setLibelle(libelleComposante);
            ufr = ufrJpaRepository.saveAndFlush(ufr);
        }
        return ufr;
    }

    public CentreGestion getCentreGestionEtab() {
        CentreGestion centreGestionEtab = centreGestionJpaRepository.getCentreEtablissement();
        // Erreur si le centre de type etablissement est null
        if (centreGestionEtab == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Centre de gestion de type établissement non trouvé");
        }
        return centreGestionEtab;
    }

    public CentreGestion getCentreGestion(CentreGestion centreGestionEtab, String codeComposante, String codeEtape, String versionEtape) {
        CentreGestion centreGestion = null;
        // Recherche du centre de gestion par codeEtape/versionEtape
        CritereGestion critereGestion = critereGestionJpaRepository.findEtapeById(codeEtape, versionEtape);
        // Si non trouvé, recherche par code composante et version = ""
        if (critereGestion == null) {
            critereGestion = critereGestionJpaRepository.findEtapeById(codeComposante, "");
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
        return centreGestion;
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
            } else {
                boolean modifiable = convention.getValidationConvention() == null || !convention.getValidationConvention();
                if (!UtilisateurHelper.isRole(utilisateur, Role.GES) && !UtilisateurHelper.isRole(utilisateur, Role.RESP_GES)) {
                    // en fonction du paramétrage
                    try {
                        modifiable = modifiable && UtilisateurHelper.isRole(utilisateur, new AppFonctionEnum[]{AppFonctionEnum.CONVENTION}, new DroitEnum[]{DroitEnum.MODIFICATION});
                    } catch (Exception e) {
                        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur technique");
                    }
                }
                return modifiable;
            }
        }
        return true;
    }

    public ResponseDto controleEmailTelephone(Convention convention) {
        // Contrôle de la présence soit du mail, soit du numéro de téléphone pour les profils liés à la signature électronique
        // Si les 2 manquantes, on affiche une erreur
        // Si une donnée est manquante on affiche un warning avec possibilité de continuer
        ResponseDto response = new ResponseDto();
        String keyMail = "adresse mail";
        String keyTel = "numéro téléphone (au format international)";
        Map<String, Map<String, String>> data = new HashMap<>();
        data.put("étudiant", new HashMap<>() {{
            put(keyMail, convention.getEtudiant().getMail());
            put(keyTel, convention.getTelPortableEtudiant());
        }});
        data.put("enseignant référent", new HashMap<>() {{
            put(keyMail, convention.getEnseignant().getMail());
            put(keyTel, convention.getEnseignant().getTel());
        }});
        data.put("tuteur pédagogique", new HashMap<>() {{
            put(keyMail, convention.getContact().getMail());
            put(keyTel, convention.getContact().getTel());
        }});
        data.put("signataire", new HashMap<>() {{
            put(keyMail, convention.getSignataire().getMail());
            put(keyTel, convention.getSignataire().getTel());
        }});
        data.put("directeur du département", new HashMap<>() {{
            put(keyMail, !Strings.isEmpty(convention.getCentreGestion().getMailDelegataireViseur()) ? convention.getCentreGestion().getMailDelegataireViseur() : convention.getCentreGestion().getMailViseur());
            put(keyTel, convention.getCentreGestion().getTelephone());
        }});

        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            Map<String, String> values = entry.getValue();
            List<String> donneesManquantes = new ArrayList<>();
            if (values.get(keyMail) == null || values.get(keyMail).isEmpty()) {
                donneesManquantes.add(keyMail);
            }
            if (values.get(keyTel) == null || values.get(keyTel).isEmpty() || parseNumTel(values.get(keyTel)) == null) {
                donneesManquantes.add(keyTel);
            }
            if (donneesManquantes.contains(keyMail)) {
                response.getError().add(entry.getKey() + " : " + String.join(", ", donneesManquantes));
            } else if (donneesManquantes.size() > 0) {
                response.getWarning().add(entry.getKey() + " : " + String.join(", ", donneesManquantes));
            }
        }
        return response;
    }

    private boolean updateNomSignataireComposante(Convention convention) {
        CentreGestion centreGestion = convention.getCentreGestion();
        if (centreGestion == null) {
            return false;
        }

        boolean hasDelegataire = !Strings.isEmpty(centreGestion.getNomDelegataireViseur()) || !Strings.isEmpty(centreGestion.getPrenomDelegataireViseur());
        String prenom = hasDelegataire ? centreGestion.getPrenomDelegataireViseur() : centreGestion.getPrenomViseur();
        String nom = hasDelegataire ? centreGestion.getNomDelegataireViseur() : centreGestion.getNomViseur();
        String qualite = hasDelegataire ? centreGestion.getQualiteDelegataireViseur() : centreGestion.getQualiteViseur();
        String fullName = buildFullName(prenom, nom);

        boolean updated = false;
        if (!Strings.isEmpty(fullName) && !fullName.equals(convention.getNomSignataireComposante())) {
            convention.setNomSignataireComposante(fullName);
            updated = true;
        }
        if (!Strings.isEmpty(qualite) && !qualite.equals(convention.getQualiteSignataire())) {
            convention.setQualiteSignataire(qualite);
            updated = true;
        }
        return updated;
    }

    private String buildFullName(String prenom, String nom) {
        StringBuilder sb = new StringBuilder();
        if (!Strings.isEmpty(prenom)) {
            sb.append(prenom.trim());
        }
        if (!Strings.isEmpty(prenom) && !Strings.isEmpty(nom)) {
            sb.append(" ");
        }
        if (!Strings.isEmpty(nom)) {
            sb.append(nom.trim());
        }
        return sb.toString().trim();
    }

    public void updateSignatureElectroniqueHistorique() {
        List<Convention> conventions = conventionJpaRepository.getSignatureInfoToUpdate();
        for (Convention convention : conventions) {
            try {
                signatureService.updateHistorique(convention);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
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

    public List<CentreGestionSignataire> initSignataires(CentreGestion centreGestion) {
        List<CentreGestionSignataire> signataires = new ArrayList<>();
        SignataireEnum[] signataireEnums = SignataireEnum.values();

        for (int i = 0; i < signataireEnums.length; i++) {
            signataires.add(new CentreGestionSignataire(centreGestion, signataireEnums[i], i + 1));
        }
        return signataires;
    }
}
