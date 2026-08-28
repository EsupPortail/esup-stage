package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.ConventionFormDto;
import org.esup_portail.esup_stage.dto.ConventionFormationDto;
import org.esup_portail.esup_stage.dto.ResponseDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.Structure.StructureService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.esup_portail.esup_stage.service.signature.SignatureService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConventionServiceTest {

    private ConventionService service;
    private ServiceJpaRepository serviceJpaRepository;
    private ContactJpaRepository contactJpaRepository;
    private StructureService structureService;
    private TypeConventionJpaRepository typeConventionJpaRepository;
    private LangueConventionJpaRepository langueConventionJpaRepository;
    private EtapeJpaRepository etapeJpaRepository;
    private UfrJpaRepository ufrJpaRepository;
    private CritereGestionJpaRepository critereGestionJpaRepository;
    private CentreGestionJpaRepository centreGestionJpaRepository;
    private ConventionJpaRepository conventionJpaRepository;
    private PaysJpaRepository paysJpaRepository;
    private EtudiantRepository etudiantRepository;
    private EtudiantJpaRepository etudiantJpaRepository;
    private AppConfigService appConfigService;
    private ApogeeService apogeeService;
    private LdapService ldapService;
    private SignatureService signatureService;
    private ConfigGeneraleDto configGenerale;

    @BeforeEach
    void setUp() {
        service = new ConventionService();
        serviceJpaRepository = mock(ServiceJpaRepository.class);
        contactJpaRepository = mock(ContactJpaRepository.class);
        structureService = mock(StructureService.class);
        typeConventionJpaRepository = mock(TypeConventionJpaRepository.class);
        langueConventionJpaRepository = mock(LangueConventionJpaRepository.class);
        etapeJpaRepository = mock(EtapeJpaRepository.class);
        ufrJpaRepository = mock(UfrJpaRepository.class);
        critereGestionJpaRepository = mock(CritereGestionJpaRepository.class);
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        paysJpaRepository = mock(PaysJpaRepository.class);
        etudiantRepository = mock(EtudiantRepository.class);
        etudiantJpaRepository = mock(EtudiantJpaRepository.class);
        appConfigService = mock(AppConfigService.class);
        apogeeService = mock(ApogeeService.class);
        ldapService = mock(LdapService.class);
        signatureService = mock(SignatureService.class);
        service.serviceJpaRepository = serviceJpaRepository;
        service.contactJpaRepository = contactJpaRepository;
        service.typeConventionJpaRepository = typeConventionJpaRepository;
        service.langueConventionJpaRepository = langueConventionJpaRepository;
        service.etapeJpaRepository = etapeJpaRepository;
        service.ufrJpaRepository = ufrJpaRepository;
        service.critereGestionJpaRepository = critereGestionJpaRepository;
        service.centreGestionJpaRepository = centreGestionJpaRepository;
        service.conventionJpaRepository = conventionJpaRepository;
        service.paysJpaRepository = paysJpaRepository;
        service.etudiantRepository = etudiantRepository;
        service.etudiantJpaRepository = etudiantJpaRepository;
        service.appConfigService = appConfigService;
        service.apogeeService = apogeeService;
        service.ldapService = ldapService;
        service.signatureService = signatureService;
        ReflectionTestUtils.setField(service, "structureService", structureService);
        HabilitationService habilitationService = mock(HabilitationService.class);
        service.habilitationService = habilitationService;
        // Rôles effectifs = rôles globaux de l'utilisateur (pas de rôle spécifique par centre dans ces tests) :
        // rend transparente l'indirection introduite par #10583 et préserve les scénarios existants.
        when(habilitationService.getEffectiveRoles(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(inv -> ((org.esup_portail.esup_stage.model.Utilisateur) inv.getArgument(0)).getRoles());

        configGenerale = new ConfigGeneraleDto();
        configGenerale.setCodeUniversite("UL");
        when(appConfigService.getConfigGenerale()).thenReturn(configGenerale);
        when(appConfigService.getAnneeUniv()).thenReturn("2025");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void connecte(String uid, String... roleCodes) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid(uid);
        utilisateur.setLogin(uid);
        utilisateur.setRoles(java.util.Arrays.stream(roleCodes).map(code -> {
            Role role = new Role();
            role.setCode(code);
            return role;
        }).toList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CasUserDetailsImpl(utilisateur, List.of()), null));
    }

    private Utilisateur utilisateur(String uid, String roleCode) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid(uid);
        utilisateur.setLogin(uid);
        Role role = new Role();
        role.setCode(roleCode);
        utilisateur.setRoles(List.of(role));
        return utilisateur;
    }

    // ------------------------------------------------------------------
    // canViewEditConvention
    // ------------------------------------------------------------------

    @Test
    void unAdminVoitToutesLesConventions() {
        assertThatCode(() -> service.canViewEditConvention(new Convention(), utilisateur("adm1", Role.ADM)))
                .doesNotThrowAnyException();
    }

    @Test
    void unEtudiantNeVoitQueSesConventions() {
        Convention convention = new Convention();
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant("Etu1");
        convention.setEtudiant(etudiant);

        assertThatCode(() -> service.canViewEditConvention(convention, utilisateur("etu1", Role.ETU)))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.canViewEditConvention(convention, utilisateur("autre", Role.ETU)))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> service.canViewEditConvention(new Convention(), utilisateur("etu1", Role.ETU)))
                .isInstanceOf(AppException.class);
    }

    @Test
    void unEnseignantNeVoitQueSesConventions() {
        Convention convention = new Convention();
        Enseignant enseignant = new Enseignant();
        enseignant.setUidEnseignant("Ens1");
        convention.setEnseignant(enseignant);

        assertThatCode(() -> service.canViewEditConvention(convention, utilisateur("ens1", Role.ENS)))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.canViewEditConvention(convention, utilisateur("autre", Role.ENS)))
                .isInstanceOf(AppException.class);

        assertThatThrownBy(() -> service.canViewEditConvention(new Convention(), utilisateur("ens1", Role.ENS)))
                .isInstanceOf(AppException.class);
    }

    @Test
    void unGestionnaireNeVoitQueLesConventionsDeSesCentres() {
        Convention convention = new Convention();
        CentreGestion centreGestion = new CentreGestion();
        PersonnelCentreGestion personnel = new PersonnelCentreGestion();
        personnel.setUidPersonnel("Ges1");
        centreGestion.setPersonnels(List.of(personnel));
        convention.setCentreGestion(centreGestion);

        assertThatCode(() -> service.canViewEditConvention(convention, utilisateur("ges1", Role.GES)))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.canViewEditConvention(convention, utilisateur("autre", Role.GES)))
                .isInstanceOf(AppException.class);

        assertThatThrownBy(() -> service.canViewEditConvention(new Convention(), utilisateur("ges1", Role.GES)))
                .isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // validationAutoDonnees
    // ------------------------------------------------------------------

    private Convention conventionTotalementValidee() {
        Convention convention = new Convention();
        convention.setValidationPedagogique(true);
        convention.setVerificationAdministrative(true);
        convention.setValidationConvention(true);
        return convention;
    }

    @Test
    void laValidationCompleteValideLesDonneesDAccueil() {
        Convention convention = conventionTotalementValidee();
        Structure structure = new Structure();
        convention.setStructure(structure);
        org.esup_portail.esup_stage.model.Service serviceAccueil = new org.esup_portail.esup_stage.model.Service();
        convention.setService(serviceAccueil);
        Contact tuteur = new Contact();
        convention.setContact(tuteur);

        service.validationAutoDonnees(convention, utilisateur("ges1", Role.GES));

        assertThat(structure.isEstValidee()).isTrue();
        assertThat(structure.getLoginValidation()).isEqualTo("ges1");
        assertThat(structure.getInfosAJour()).isNotNull();
        verify(structureService).save(any(), any(Structure.class));
        assertThat(serviceAccueil.getInfosAJour()).isNotNull();
        verify(serviceJpaRepository).save(serviceAccueil);
        assertThat(tuteur.getInfosAJour()).isNotNull();
        verify(contactJpaRepository).save(tuteur);
    }

    @Test
    void sansValidationCompleteRienNEstTouche() {
        Convention convention = new Convention();
        convention.setValidationPedagogique(true);
        convention.setVerificationAdministrative(true);
        convention.setValidationConvention(false);
        convention.setStructure(new Structure());

        service.validationAutoDonnees(convention, utilisateur("ges1", Role.GES));

        verify(structureService, never()).save(any(), any(Structure.class));
        verify(serviceJpaRepository, never()).save(any());
        verify(contactJpaRepository, never()).save(any());
    }

    @Test
    void laValidationCompleteToleredLesDonneesAbsentes() {
        assertThatCode(() -> service.validationAutoDonnees(conventionTotalementValidee(), utilisateur("ges1", Role.GES)))
                .doesNotThrowAnyException();
    }

    // ------------------------------------------------------------------
    // parseNumTel / initSignataires
    // ------------------------------------------------------------------

    @Test
    void lesNumerosFrancaisSontNormalisesEnE164() {
        assertThat(service.parseNumTel("06 11 22 33 44")).isEqualTo("+33611223344");
        assertThat(service.parseNumTel("+33 6 11 22 33 44")).isEqualTo("+33611223344");
        assertThat(service.parseNumTel("0000")).isNull();
        assertThat(service.parseNumTel("pas un numero")).isNull();
        assertThat(service.parseNumTel("")).isNull();
        assertThat(service.parseNumTel(null)).isNull();
    }

    @Test
    void parseNumTelKeepsValidFixedLineNumbersForGeneralUse() {
        assertThat(service.parseNumTel("0182280026")).isEqualTo("+33182280026");
    }

    @Test
    void parseNumTelMobileRejectsFixedLineNumbersRejectedByDocaposteOtp() {
        assertThat(service.parseNumTelMobile("0182280026")).isNull();
        assertThat(service.parseNumTelMobile("0596782950")).isNull();
        assertThat(service.parseNumTelMobile("0188321350")).isNull();
    }

    @Test
    void parseNumTelMobileFormatsValidMobileNumbers() {
        assertThat(service.parseNumTelMobile("0612345678")).isEqualTo("+33612345678");
    }

    @Test
    void initSignatairesCreeUnSignataireParTypeDansLOrdre() {
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(3);

        List<CentreGestionSignataire> signataires = service.initSignataires(centreGestion);

        assertThat(signataires).hasSize(5);
        assertThat(signataires.get(0).getOrdre()).isEqualTo(1);
        assertThat(signataires.get(4).getOrdre()).isEqualTo(5);
        assertThat(signataires)
                .allSatisfy(s -> assertThat(s.getCentreGestion()).isSameAs(centreGestion));
    }

    // ------------------------------------------------------------------
    // nomenclatures
    // ------------------------------------------------------------------

    @Test
    void lesNomenclaturesIntrouvablesSontRejetees() {
        when(typeConventionJpaRepository.findById(3)).thenReturn(null);
        assertThatThrownBy(() -> service.getTypeConvention(3)).isInstanceOf(AppException.class);
        when(typeConventionJpaRepository.findById(3)).thenReturn(new TypeConvention());
        assertThat(service.getTypeConvention(3)).isNotNull();

        when(langueConventionJpaRepository.findByCode("fr")).thenReturn(null);
        assertThatThrownBy(() -> service.getLangueConvention("fr")).isInstanceOf(AppException.class);
        when(langueConventionJpaRepository.findByCode("fr")).thenReturn(new LangueConvention());
        assertThat(service.getLangueConvention("fr")).isNotNull();

        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(null);
        assertThatThrownBy(() -> service.getCentreGestionEtab()).isInstanceOf(AppException.class);
        CentreGestion etab = new CentreGestion();
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(etab);
        assertThat(service.getCentreGestionEtab()).isSameAs(etab);
    }

    @Test
    void getEtapeCreeOuCompleteLEtape() {
        when(etapeJpaRepository.saveAndFlush(any(Etape.class))).thenAnswer(inv -> inv.getArgument(0));

        // étape inconnue : création
        when(etapeJpaRepository.findById("M1", "1", "UL")).thenReturn(null);
        Etape creee = service.getEtape("M1", "1", "Master 1");
        assertThat(creee.getLibelle()).isEqualTo("Master 1");
        assertThat(creee.getId().getCodeUniversite()).isEqualTo("UL");

        // étape connue sans libellé : complétée
        Etape sansLibelle = new Etape();
        when(etapeJpaRepository.findById("M1", "1", "UL")).thenReturn(sansLibelle);
        assertThat(service.getEtape("M1", "1", "Master 1").getLibelle()).isEqualTo("Master 1");

        // étape connue avec libellé : inchangée
        Etape complete = new Etape();
        complete.setLibelle("Existant");
        when(etapeJpaRepository.findById("M1", "1", "UL")).thenReturn(complete);
        assertThat(service.getEtape("M1", "1", "Master 1").getLibelle()).isEqualTo("Existant");
    }

    @Test
    void getUfrCreeLaComposanteInconnue() {
        when(ufrJpaRepository.saveAndFlush(any(Ufr.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ufrJpaRepository.findById("SCI", "UL")).thenReturn(null);
        assertThat(service.getUfr("SCI", "Sciences").getLibelle()).isEqualTo("Sciences");

        Ufr existante = new Ufr();
        when(ufrJpaRepository.findById("SCI", "UL")).thenReturn(existante);
        assertThat(service.getUfr("SCI", "Sciences")).isSameAs(existante);
    }

    @Test
    void leCentreDeGestionEstResoluParEtapePuisComposante() {
        CentreGestion etab = new CentreGestion();
        CentreGestion centreEtape = new CentreGestion();
        CritereGestion critereEtape = new CritereGestion();
        critereEtape.setCentreGestion(centreEtape);

        // trouvé par le couple étape/version
        when(critereGestionJpaRepository.findEtapeById("M1", "1")).thenReturn(critereEtape);
        assertThat(service.getCentreGestion(etab, "SCI", "M1", "1")).isSameAs(centreEtape);

        // sinon par la composante
        when(critereGestionJpaRepository.findEtapeById("M1", "1")).thenReturn(null);
        when(critereGestionJpaRepository.findEtapeById("SCI", "")).thenReturn(critereEtape);
        assertThat(service.getCentreGestion(etab, "SCI", "M1", "1")).isSameAs(centreEtape);

        // aucun critère et conventions orphelines interdites : erreur
        when(critereGestionJpaRepository.findEtapeById("SCI", "")).thenReturn(null);
        assertThatThrownBy(() -> service.getCentreGestion(etab, "SCI", "M1", "1")).isInstanceOf(AppException.class);

        // conventions orphelines autorisées : repli sur le centre établissement
        configGenerale.setAutoriserConventionsOrphelines(true);
        assertThat(service.getCentreGestion(etab, "SCI", "M1", "1")).isSameAs(etab);

        // critère trouvé mais sans centre : erreur
        when(critereGestionJpaRepository.findEtapeById("M1", "1")).thenReturn(new CritereGestion());
        assertThatThrownBy(() -> service.getCentreGestion(etab, "SCI", "M1", "1")).isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // isConventionModifiable
    // ------------------------------------------------------------------

    @Test
    void laModifiabiliteDependDuRoleEtDesValidations() {
        Convention convention = new Convention();
        CentreGestion centre = new CentreGestion();
        convention.setCentreGestion(centre);

        // admin : toujours modifiable
        assertThat(service.isConventionModifiable(convention, utilisateur("adm1", Role.ADM))).isTrue();

        // étudiant : bloqué dès qu'une validation activée sur le centre est posée
        Utilisateur etu = utilisateur("etu1", Role.ETU);
        assertThat(service.isConventionModifiable(convention, etu)).isTrue();
        centre.setValidationConvention(true);
        convention.setValidationConvention(true);
        assertThat(service.isConventionModifiable(convention, etu)).isFalse();
        centre.setValidationConvention(false);
        centre.setVerificationAdministrative(true);
        convention.setVerificationAdministrative(true);
        assertThat(service.isConventionModifiable(convention, etu)).isFalse();
        centre.setVerificationAdministrative(false);
        centre.setValidationPedagogique(true);
        convention.setValidationPedagogique(true);
        assertThat(service.isConventionModifiable(convention, etu)).isFalse();

        // gestionnaire : bloqué une fois la convention validée
        Utilisateur ges = utilisateur("ges1", Role.GES);
        assertThat(service.isConventionModifiable(convention, ges)).isFalse();
        convention.setValidationConvention(false);
        assertThat(service.isConventionModifiable(convention, ges)).isTrue();
    }

    @Test
    void laModifiabiliteDUnEnseignantSuitSesHabilitations() {
        Convention convention = new Convention();

        Utilisateur ens = utilisateur("ens1", Role.ENS);
        RoleAppFonction habilitation = new RoleAppFonction();
        AppFonction fonction = new AppFonction();
        fonction.setCode(AppFonctionEnum.CONVENTION);
        habilitation.setAppFonction(fonction);
        habilitation.setModification(true);
        ens.getRoles().get(0).setRoleAppFonctions(List.of(habilitation));
        assertThat(service.isConventionModifiable(convention, ens)).isTrue();

        habilitation.setModification(false);
        assertThat(service.isConventionModifiable(convention, ens)).isFalse();

        // rôle sans habilitations chargées : erreur technique
        Utilisateur incomplet = utilisateur("ens2", Role.ENS);
        assertThatThrownBy(() -> service.isConventionModifiable(convention, incomplet))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("technique");
    }

    // ------------------------------------------------------------------
    // contrôle mail/téléphone avant signature
    // ------------------------------------------------------------------

    private Convention conventionSignature() {
        Convention convention = new Convention();
        Etudiant etudiant = new Etudiant();
        etudiant.setMail("etu@univ.fr");
        convention.setEtudiant(etudiant);
        convention.setTelPortableEtudiant("0601020304");
        Enseignant enseignant = new Enseignant();
        enseignant.setMail("ens@univ.fr");
        enseignant.setTel("0301020304");
        convention.setEnseignant(enseignant);
        Contact tuteur = new Contact();
        tuteur.setMail("tuteur@acme.fr");
        tuteur.setTel("0201020304");
        convention.setContact(tuteur);
        Contact signataire = new Contact();
        signataire.setMail("dir@acme.fr");
        signataire.setTel("0101020304");
        convention.setSignataire(signataire);
        CentreGestion centre = new CentreGestion();
        centre.setMailViseur("viseur@univ.fr");
        centre.setTelephone("0901020304");
        convention.setCentreGestion(centre);
        return convention;
    }

    @Test
    void leControleDesCoordonneesDistingueErreursEtAvertissements() {
        // toutes les données présentes : aucun problème
        ResponseDto complet = service.controleEmailTelephone(conventionSignature());
        assertThat(complet.getError()).isEmpty();
        assertThat(complet.getWarning()).isEmpty();

        // mail manquant : erreur ; téléphone invalide : avertissement
        Convention incomplet = conventionSignature();
        incomplet.getEtudiant().setMail(null);
        incomplet.getEnseignant().setTel("123");
        ResponseDto reponse = service.controleEmailTelephone(incomplet);
        assertThat(reponse.getError()).anyMatch(m -> m.startsWith("étudiant"));
        assertThat(reponse.getWarning()).anyMatch(m -> m.startsWith("enseignant référent"));

        // le mail du délégataire remplace celui du viseur
        Convention delegataire = conventionSignature();
        delegataire.getCentreGestion().setMailDelegataireViseur("delegataire@univ.fr");
        assertThat(service.controleEmailTelephone(delegataire).getError()).isEmpty();
    }

    // ------------------------------------------------------------------
    // synchronisation du signataire de composante + historique signature
    // ------------------------------------------------------------------

    @Test
    void leNomDuSignataireDeComposanteEstSynchronise() {
        Convention convention = new Convention();
        convention.setValidationPedagogique(true);
        convention.setVerificationAdministrative(true);
        convention.setValidationConvention(true);
        CentreGestion centre = new CentreGestion();
        centre.setPrenomViseur("Anne");
        centre.setNomViseur("Bertrand");
        centre.setQualiteViseur("Directrice");
        convention.setCentreGestion(centre);

        service.validationAutoDonnees(convention, utilisateur("ges1", Role.GES));

        assertThat(convention.getNomSignataireComposante()).isEqualTo("Anne Bertrand");
        assertThat(convention.getQualiteSignataire()).isEqualTo("Directrice");
        verify(conventionJpaRepository).save(convention);

        // le délégataire prime sur le viseur
        centre.setNomDelegataireViseur("Claude");
        centre.setQualiteDelegataireViseur("Doyen");
        service.validationAutoDonnees(convention, utilisateur("ges1", Role.GES));
        assertThat(convention.getNomSignataireComposante()).isEqualTo("Claude");
        assertThat(convention.getQualiteSignataire()).isEqualTo("Doyen");
    }

    @Test
    void lHistoriqueDeSignatureContinueMalgreLesErreurs() {
        Convention enErreur = new Convention();
        enErreur.setId(1);
        Convention suivante = new Convention();
        suivante.setId(2);
        when(conventionJpaRepository.getSignatureInfoToUpdate()).thenReturn(List.of(enErreur, suivante));
        org.mockito.Mockito.doThrow(new RuntimeException("api down")).when(signatureService).updateHistorique(enErreur);

        service.updateSignatureElectroniqueHistorique();

        verify(signatureService).updateHistorique(suivante);
    }

    // ------------------------------------------------------------------
    // setConventionData
    // ------------------------------------------------------------------

    private ConventionFormDto formulaireConvention() {
        ConventionFormDto dto = new ConventionFormDto();
        dto.setEtudiantLogin("etu1");
        dto.setIdTypeConvention(3);
        dto.setCodeLangueConvention("fr");
        dto.setNumEtudiant("12345");
        dto.setCodeEtape("M1");
        dto.setCodeVersionEtape("1");
        dto.setLibelleEtape("Master 1");
        dto.setCodeComposante("SCI");
        dto.setLibelleComposante("Sciences");
        dto.setAnnee("2025");
        dto.setAdresseEtudiant("1 rue des Lilas");
        return dto;
    }

    private void stubDonneesEtudiant() {
        when(typeConventionJpaRepository.findById(3)).thenReturn(new TypeConvention());
        when(langueConventionJpaRepository.findByCode("fr")).thenReturn(new LangueConvention());
        EtudiantRef etudiantRef = mock(EtudiantRef.class);
        when(etudiantRef.getNompatro()).thenReturn("Dupont");
        when(etudiantRef.getPrenom()).thenReturn("Marie");
        when(etudiantRef.getMail()).thenReturn("marie@univ.fr");
        when(apogeeService.getInfoApogee("12345", "2025")).thenReturn(etudiantRef);
        LdapUser ldapUser = mock(LdapUser.class);
        when(ldapUser.getUid()).thenReturn("etu1");
        when(ldapService.search(eq("/etudiant"), any())).thenReturn(List.of(ldapUser));
        when(etapeJpaRepository.saveAndFlush(any(Etape.class))).thenAnswer(inv -> inv.getArgument(0));
        when(etapeJpaRepository.findById("M1", "1", "UL")).thenReturn(new Etape());
        when(ufrJpaRepository.findById("SCI", "UL")).thenReturn(new Ufr());
        CentreGestion etab = new CentreGestion();
        etab.setNomCentre("Université");
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(etab);
        CentreGestion centre = new CentreGestion();
        CritereGestion critere = new CritereGestion();
        critere.setCentreGestion(centre);
        when(critereGestionJpaRepository.findEtapeById("M1", "1")).thenReturn(critere);
        when(etudiantRepository.findByNumEtudiant("12345")).thenReturn(null);
        when(etudiantJpaRepository.saveAndFlush(any(Etudiant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paysJpaRepository.findByIso2("FR")).thenReturn(new Pays());
    }

    private void stubInscriptionEligible() {
        ConventionFormationDto inscription = mock(ConventionFormationDto.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        when(inscription.getAnnee()).thenReturn("2025");
        when(inscription.getEtapeInscription().getCodeEtp()).thenReturn("M1");
        when(inscription.getEtapeInscription().getCodVrsVet()).thenReturn("1");
        when(apogeeService.getInscriptions(any(), eq("12345"), eq("2025"))).thenReturn(List.of(inscription));
    }

    @Test
    void setConventionDataAlimenteLaConventionComplete() {
        connecte("ges1", Role.GES);
        stubDonneesEtudiant();
        stubInscriptionEligible();

        Convention convention = new Convention();
        service.setConventionData(convention, formulaireConvention());

        assertThat(convention.getEtudiant().getNom()).isEqualTo("Dupont");
        assertThat(convention.getEtudiant().getPrenom()).isEqualTo("Marie");
        assertThat(convention.getEtudiant().getIdentEtudiant()).isEqualTo("etu1");
        assertThat(convention.getEtudiant().getCodeUniversite()).isEqualTo("UL");
        assertThat(convention.getPaysConvention()).isNotNull();
        assertThat(convention.getAnnee()).isEqualTo("2025/2026");
        assertThat(convention.getNomEtabRef()).isEqualTo("Université");
        assertThat(convention.getAdresseEtudiant()).isEqualTo("1 rue des Lilas");
    }

    @Test
    void setConventionDataControleLesPrerequis() {
        ConventionFormDto dto = formulaireConvention();

        // un étudiant ne modifie que ses propres conventions
        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> service.setConventionData(new Convention(), dto)).isInstanceOf(AppException.class);

        // convention absente
        connecte("ges1", Role.GES);
        assertThatThrownBy(() -> service.setConventionData(null, dto)).isInstanceOf(AppException.class);

        // étudiant introuvable dans Apogée
        when(typeConventionJpaRepository.findById(3)).thenReturn(new TypeConvention());
        when(langueConventionJpaRepository.findByCode("fr")).thenReturn(new LangueConvention());
        when(apogeeService.getInfoApogee("12345", "2025")).thenReturn(null);
        assertThatThrownBy(() -> service.setConventionData(new Convention(), dto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Étudiant");

        // étudiant absent du LDAP
        when(apogeeService.getInfoApogee("12345", "2025")).thenReturn(mock(EtudiantRef.class));
        when(ldapService.search(eq("/etudiant"), any())).thenReturn(List.of());
        assertThatThrownBy(() -> service.setConventionData(new Convention(), dto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Étudiant");
    }

    @Test
    void uneFormationNonEligibleEstRefusee() {
        connecte("ges1", Role.GES);
        stubDonneesEtudiant();

        // aucune inscription correspondante
        when(apogeeService.getInscriptions(any(), eq("12345"), eq("2025"))).thenReturn(List.of());
        ConventionFormDto dto = formulaireConvention();
        assertThatThrownBy(() -> service.setConventionData(new Convention(), dto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("pas autorisé");

        // inscription sans étape : ignorée
        ConventionFormationDto sansEtape = mock(ConventionFormationDto.class);
        when(apogeeService.getInscriptions(any(), eq("12345"), eq("2025"))).thenReturn(List.of(sansEtape));
        assertThatThrownBy(() -> service.setConventionData(new Convention(), dto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("pas autorisé");
    }
}
