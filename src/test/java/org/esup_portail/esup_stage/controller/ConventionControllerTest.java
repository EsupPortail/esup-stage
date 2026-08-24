package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.dto.AccordAnnuaireDto;
import org.esup_portail.esup_stage.dto.AnneeUniversitaireDto;
import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.ConventionFormDto;
import org.esup_portail.esup_stage.dto.ConventionSingleFieldDto;
import org.esup_portail.esup_stage.dto.DateStageDto;
import org.esup_portail.esup_stage.dto.IdsListDto;
import org.esup_portail.esup_stage.dto.MetadataDto;
import org.esup_portail.esup_stage.dto.PeriodesDto;
import org.esup_portail.esup_stage.dto.ResponseDto;
import org.esup_portail.esup_stage.enums.AppSignatureEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConventionDocumentEtudiantService;
import org.esup_portail.esup_stage.service.ConventionService;
import org.esup_portail.esup_stage.service.MailerService;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.esup_portail.esup_stage.service.signature.SignatureService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConventionControllerTest {

    private ConventionController controller;
    private ConventionRepository conventionRepository;
    private ConventionJpaRepository conventionJpaRepository;
    private AppConfigService appConfigService;
    private ConventionService conventionService;
    private org.esup_portail.esup_stage.service.HabilitationService habilitationService;
    private MailerService mailerService;
    private ImpressionService impressionService;
    private ConventionDocumentEtudiantService conventionDocumentEtudiantService;
    private SignatureService signatureService;
    private SignatureProperties signatureProperties;
    private CentreGestionJpaRepository centreGestionJpaRepository;
    private HistoriqueValidationJpaRepository historiqueValidationJpaRepository;
    private AvenantJpaRepository avenantJpaRepository;
    private LangueConventionJpaRepository langueConventionJpaRepository;
    private PaysJpaRepository paysJpaRepository;
    private TypeConventionJpaRepository typeConventionJpaRepository;
    private ThemeJpaRepository themeJpaRepository;
    private TempsTravailJpaRepository tempsTravailJpaRepository;
    private UniteGratificationJpaRepository uniteGratificationJpaRepository;
    private UniteDureeJpaRepository uniteDureeJpaRepository;
    private DeviseJpaRepository deviseJpaRepository;
    private ModeVersGratificationJpaRepository modeVersGratificationJpaRepository;
    private OrigineStageJpaRepository origineStageJpaRepository;
    private NatureTravailJpaRepository natureTravailJpaRepository;
    private ModeValidationStageJpaRepository modeValidationStageJpaRepository;
    private StructureJpaRepository structureJpaRepository;
    private ServiceJpaRepository serviceJpaRepository;
    private ContactJpaRepository contactJpaRepository;
    private EnseignantJpaRepository enseignantJpaRepository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        controller = new ConventionController();
        conventionRepository = mock(ConventionRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        appConfigService = mock(AppConfigService.class);
        conventionService = mock(ConventionService.class);
        habilitationService = mock(org.esup_portail.esup_stage.service.HabilitationService.class);
        mailerService = mock(MailerService.class);
        impressionService = mock(ImpressionService.class);
        conventionDocumentEtudiantService = mock(ConventionDocumentEtudiantService.class);
        signatureService = mock(SignatureService.class);
        signatureProperties = mock(SignatureProperties.class);
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        historiqueValidationJpaRepository = mock(HistoriqueValidationJpaRepository.class);
        avenantJpaRepository = mock(AvenantJpaRepository.class);
        langueConventionJpaRepository = mock(LangueConventionJpaRepository.class);
        paysJpaRepository = mock(PaysJpaRepository.class);
        typeConventionJpaRepository = mock(TypeConventionJpaRepository.class);
        themeJpaRepository = mock(ThemeJpaRepository.class);
        tempsTravailJpaRepository = mock(TempsTravailJpaRepository.class);
        uniteGratificationJpaRepository = mock(UniteGratificationJpaRepository.class);
        uniteDureeJpaRepository = mock(UniteDureeJpaRepository.class);
        deviseJpaRepository = mock(DeviseJpaRepository.class);
        modeVersGratificationJpaRepository = mock(ModeVersGratificationJpaRepository.class);
        origineStageJpaRepository = mock(OrigineStageJpaRepository.class);
        natureTravailJpaRepository = mock(NatureTravailJpaRepository.class);
        modeValidationStageJpaRepository = mock(ModeValidationStageJpaRepository.class);
        structureJpaRepository = mock(StructureJpaRepository.class);
        serviceJpaRepository = mock(ServiceJpaRepository.class);
        contactJpaRepository = mock(ContactJpaRepository.class);
        enseignantJpaRepository = mock(EnseignantJpaRepository.class);

        controller.conventionRepository = conventionRepository;
        controller.conventionJpaRepository = conventionJpaRepository;
        controller.appConfigService = appConfigService;
        controller.conventionService = conventionService;
        controller.habilitationService = habilitationService;
        controller.mailerService = mailerService;
        controller.impressionService = impressionService;
        controller.conventionDocumentEtudiantService = conventionDocumentEtudiantService;
        controller.signatureService = signatureService;
        controller.signatureProperties = signatureProperties;
        controller.centreGestionJpaRepository = centreGestionJpaRepository;
        controller.historiqueValidationJpaRepository = historiqueValidationJpaRepository;
        controller.avenantJpaRepository = avenantJpaRepository;
        controller.langueConventionJpaRepository = langueConventionJpaRepository;
        controller.paysJpaRepository = paysJpaRepository;
        controller.typeConventionJpaRepository = typeConventionJpaRepository;
        controller.themeJpaRepository = themeJpaRepository;
        controller.tempsTravailJpaRepository = tempsTravailJpaRepository;
        controller.uniteGratificationJpaRepository = uniteGratificationJpaRepository;
        controller.uniteDureeJpaRepository = uniteDureeJpaRepository;
        controller.deviseJpaRepository = deviseJpaRepository;
        controller.modeVersGratificationJpaRepository = modeVersGratificationJpaRepository;
        controller.origineStageJpaRepository = origineStageJpaRepository;
        controller.natureTravailJpaRepository = natureTravailJpaRepository;
        controller.modeValidationStageJpaRepository = modeValidationStageJpaRepository;
        controller.structureJpaRepository = structureJpaRepository;
        controller.serviceJpaRepository = serviceJpaRepository;
        controller.contactJpaRepository = contactJpaRepository;
        controller.enseignantJpaRepository = enseignantJpaRepository;

        when(conventionJpaRepository.saveAndFlush(any(Convention.class))).thenAnswer(inv -> inv.getArgument(0));
        when(appConfigService.getConfigAlerteMail()).thenReturn(new ConfigAlerteMailDto());
        when(appConfigService.getAnneeUniv()).thenReturn("2025");
        when(appConfigService.getAnneeUnivFromLibelle(any())).thenAnswer(inv -> inv.getArgument(0, String.class).split("/")[0]);
        when(appConfigService.getAnneeUnivLibelle(any())).thenAnswer(inv -> inv.getArgument(0) + "/" + (Integer.parseInt(inv.getArgument(0)) + 1));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Utilisateur connecte(String uid, String... roleCodes) {
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
        return utilisateur;
    }

    /** setConventionData mocké : pose le minimum requis par setValeurNomenclature(). */
    private void stubSetConventionData() {
        doAnswer(inv -> {
            Convention convention = inv.getArgument(0);
            LangueConvention langue = new LangueConvention();
            langue.setLibelle("Français");
            convention.setLangueConvention(langue);
            TypeConvention type = new TypeConvention();
            type.setLibelle("Stage");
            convention.setTypeConvention(type);
            return null;
        }).when(conventionService).setConventionData(any(Convention.class), any(ConventionFormDto.class));
    }

    // ------------------------------------------------------------------
    // brouillon et années
    // ------------------------------------------------------------------

    @Test
    void brouillonExistantOuNeuf() {
        connecte("etu1", Role.ETU);
        Convention brouillon = new Convention();
        when(conventionJpaRepository.findBrouillon("etu1")).thenReturn(brouillon);
        assertThat(controller.getBrouillon()).isSameAs(brouillon);

        when(conventionJpaRepository.findBrouillon("etu1")).thenReturn(null);
        assertThat(controller.getBrouillon()).isNotNull();
    }

    @Test
    void lesAnneesDependentDuRole() {
        connecte("adm1", Role.ADM);
        when(conventionJpaRepository.getAnnees()).thenReturn(List.of("2024/2025", "2025/2026"));
        List<AnneeUniversitaireDto> annees = controller.getListAnnees();
        assertThat(annees).hasSize(2);
        assertThat(annees.stream().filter(AnneeUniversitaireDto::isAnneeEnCours))
                .extracting(AnneeUniversitaireDto::getAnnee).containsExactly("2025");

        connecte("ges1", Role.GES);
        when(habilitationService.getAuthorizedCentreIds(any(), any(), any())).thenReturn(List.of(3));
        when(conventionJpaRepository.getAnneesByCentreIds(List.of(3))).thenReturn(List.of("2025/2026"));
        assertThat(controller.getListAnnees()).hasSize(1);

        connecte("ens1", Role.ENS);
        when(conventionJpaRepository.getEnseignantAnnees("ens1")).thenReturn(List.of("2025/2026"));
        assertThat(controller.getListAnnees()).hasSize(1);

        connecte("etu1", Role.ETU);
        when(conventionJpaRepository.getEtudiantAnnees("etu1")).thenReturn(List.of("2025/2026"));
        assertThat(controller.getListAnnees()).hasSize(1);
    }

    @Test
    void lAnneeEnCoursEstAjouteeSiAbsente() {
        connecte("adm1", Role.ADM);
        when(conventionJpaRepository.getAnnees()).thenReturn(List.of("2023/2024"));

        List<AnneeUniversitaireDto> annees = controller.getListAnnees();

        assertThat(annees).hasSize(2);
        assertThat(annees.get(1).getAnnee()).isEqualTo("2025");
        assertThat(annees.get(1).isAnneeEnCours()).isTrue();
    }

    // ------------------------------------------------------------------
    // search / exports : injection du filtre selon le rôle
    // ------------------------------------------------------------------

    @Test
    void laRechercheInjecteLeFiltreSelonLeRole() {
        when(conventionRepository.count(anyString())).thenReturn(0L);
        when(conventionRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());

        connecte("etu1", Role.ETU);
        controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());
        ArgumentCaptor<String> filtres = ArgumentCaptor.forClass(String.class);
        verify(conventionRepository).count(filtres.capture());
        assertThat(filtres.getValue()).contains("userScope").contains("\"uid\":\"etu1\"").contains("\"etudiant\":true");

        connecte("ens1", Role.ENS);
        controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());
        connecte("ges1", Role.GES);
        controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());
        connecte("adm1", Role.ADM);
        controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());

        List<String> tous = ArgumentCaptor.forClass(String.class).getAllValues();
        ArgumentCaptor<String> tousFiltres = ArgumentCaptor.forClass(String.class);
        verify(conventionRepository, org.mockito.Mockito.times(4)).count(tousFiltres.capture());
        assertThat(tousFiltres.getAllValues().get(1)).contains("\"enseignant\":true").contains("\"uid\":\"ens1\"");
        assertThat(tousFiltres.getAllValues().get(2)).contains("userScope").contains("\"uid\":\"ges1\"");
        // L'admin n'a aucune restriction de périmètre, mais les conventions archivées restent
        // masquées tant qu'il ne les demande pas explicitement.
        assertThat(tousFiltres.getAllValues().get(3))
                .doesNotContain("userScope")
                .contains("\"archive\"")
                .contains("\"value\":false");

        // Seul l'admin peut lever ce masquage, en demandant explicitement les conventions archivées
        controller.search(1, 50, "id", "asc", "{\"archive\":{\"specific\":true,\"value\":true}}", new MockHttpServletResponse());
        ArgumentCaptor<String> filtresAvecArchive = ArgumentCaptor.forClass(String.class);
        verify(conventionRepository, org.mockito.Mockito.times(5)).count(filtresAvecArchive.capture());
        assertThat(filtresAvecArchive.getAllValues().get(4)).contains("\"value\":true");
    }

    @Test
    void lesExportsDeleguentAuRepository() {
        connecte("adm1", Role.ADM);
        // Le filtre transmis au repository est enrichi selon le rôle : on ne le contraint pas ici
        when(conventionRepository.exportExcel(eq("{}"), eq("id"), eq("asc"), anyString())).thenReturn(new byte[]{1});
        when(conventionRepository.exportCsv(eq("{}"), eq("id"), eq("asc"), anyString())).thenReturn(new StringBuilder("csv"));

        assertThat(controller.exportExcel("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody())
                .containsExactly((byte) 1);
        assertThat(controller.exportCsv("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody())
                .isEqualTo("csv");

        // Les exports appliquent le même masquage que la recherche : pas de convention archivée
        ArgumentCaptor<String> filtres = ArgumentCaptor.forClass(String.class);
        verify(conventionRepository).exportExcel(anyString(), anyString(), anyString(), filtres.capture());
        assertThat(filtres.getValue()).contains("\"archive\"").contains("\"value\":false");
    }

    // ------------------------------------------------------------------
    // getById
    // ------------------------------------------------------------------

    @Test
    void getByIdControleLAccesEtudiant() {
        Convention convention = new Convention();
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant("etu1");
        convention.setEtudiant(etudiant);
        when(conventionJpaRepository.findById(42)).thenReturn(convention);

        connecte("etu1", Role.ETU);
        assertThat(controller.getById(42)).isSameAs(convention);

        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> controller.getById(42))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getById(99)).isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // create / update
    // ------------------------------------------------------------------

    @Test
    void createRefuseUnDeuxiemeBrouillon() {
        connecte("etu1", Role.ETU);
        when(conventionJpaRepository.findBrouillon("etu1")).thenReturn(new Convention());

        assertThatThrownBy(() -> controller.create(new ConventionFormDto()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("déjà une convention");
    }

    @Test
    void createInitialiseLaConventionEtSaNomenclature() {
        connecte("etu1", Role.ETU);
        when(conventionJpaRepository.findBrouillon("etu1")).thenReturn(null);
        stubSetConventionData();

        Convention convention = controller.create(new ConventionFormDto());

        assertThat(convention.isValidationCreation()).isFalse();
        assertThat(convention.getNomenclature().getLangueConvention()).isEqualTo("Français");
        assertThat(convention.getNomenclature().getTypeConvention()).isEqualTo("Stage");
        verify(conventionJpaRepository).saveAndFlush(convention);
    }

    @Test
    void updateEchoueSiConventionInconnue() {
        connecte("etu1", Role.ETU);
        when(conventionJpaRepository.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> controller.update(99, new ConventionFormDto()))
                .isInstanceOf(AppException.class);
    }

    @Test
    void updateDUnBrouillonNEnvoiePasDeMail() {
        connecte("etu1", Role.ETU);
        Convention convention = new Convention();
        convention.setValidationCreation(false);
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        stubSetConventionData();

        controller.update(42, new ConventionFormDto());

        verify(mailerService, never()).sendValidationMail(any(), any(), any(Utilisateur.class),
                any(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    // ------------------------------------------------------------------
    // validation / dévalidation
    // ------------------------------------------------------------------

    private Convention conventionValidable() {
        Convention convention = new Convention();
        convention.setId(42);
        LangueConvention langue = new LangueConvention();
        langue.setLibelle("Français");
        convention.setLangueConvention(langue);
        TypeConvention type = new TypeConvention();
        type.setLibelle("Stage");
        convention.setTypeConvention(type);
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setValidationPedagogique(true);
        convention.setCentreGestion(centreGestion);
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        return convention;
    }

    @Test
    void validerChaqueTypeBasculeLeFlagCorrespondant() {
        connecte("ges1", Role.GES);

        Convention convention = conventionValidable();
        controller.validate(42, "validationConvention");
        assertThat(convention.getValidationConvention()).isTrue();
        assertThat(convention.getLoginValidation()).isEqualTo("ges1");

        controller.validate(42, "validationPedagogique");
        assertThat(convention.getValidationPedagogique()).isTrue();

        controller.validate(42, "verificationAdministrative");
        assertThat(convention.getVerificationAdministrative()).isTrue();

        controller.unvalidate(42, "validationConvention");
        assertThat(convention.getValidationConvention()).isFalse();

        verify(conventionService, org.mockito.Mockito.times(4)).canViewEditConvention(eq(convention), any(), any());
    }

    @Test
    void validerUnTypeInconnuEstRefuse() {
        connecte("ges1", Role.GES);
        conventionValidable();

        assertThatThrownBy(() -> controller.validate(42, "typeFarfelu"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Type de validation inconnu");
        assertThatThrownBy(() -> controller.unvalidate(42, "typeFarfelu"))
                .isInstanceOf(AppException.class);
    }

    @Test
    void unEnseignantNeValideQueLaPedagogie() {
        connecte("ens1", Role.ENS);
        conventionValidable();
        // La restriction « enseignant = validation pédagogique uniquement » est portée par
        // ConventionService.checkValidationType (mocké ici) : on simule son refus.
        org.mockito.Mockito.doThrow(new AppException(org.springframework.http.HttpStatus.BAD_REQUEST, "Type de validation inconnu"))
                .when(conventionService).checkValidationType(any(Convention.class), any(), org.mockito.ArgumentMatchers.anyString());

        assertThatThrownBy(() -> controller.validate(42, "validationConvention"))
                .isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.unvalidate(42, "verificationAdministrative"))
                .isInstanceOf(AppException.class);
    }

    @Test
    void validerUneConventionInconnueEchoue() {
        connecte("ges1", Role.GES);
        when(conventionJpaRepository.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> controller.validate(99, "validationConvention"))
                .isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.unvalidate(99, "validationConvention"))
                .isInstanceOf(AppException.class);
    }

    @Test
    void laValidationAdministrativeMultipleValideLesConventionsEligibles() {
        connecte("ges1", Role.GES);

        Convention eligible = conventionValidable();
        eligible.setValidationPedagogique(true);

        IdsListDto ids = new IdsListDto();
        ids.setIds(List.of(42));

        var reponse = controller.validationAdministrativeMultiple(ids);

        assertThat(reponse.getBody()).containsEntry("message", "1 convention(s) validée(s)");
        assertThat(eligible.getValidationConvention()).isTrue();
        verify(conventionService).validationAutoDonnees(eq(eligible), any());
    }

    @Test
    void laValidationMultipleSignaleLesConventionsSansValidationPedagogique() {
        connecte("ges1", Role.GES);
        Convention nonEligible = conventionValidable();
        nonEligible.setValidationPedagogique(false);

        IdsListDto ids = new IdsListDto();
        ids.setIds(List.of(42));

        var reponse = controller.validationAdministrativeMultiple(ids);

        assertThat(reponse.getBody().get("message")).contains("n'a pas pu être validée administrativement");
    }

    @Test
    void laValidationMultipleControleLesEntrees() {
        connecte("ens1", Role.ENS);
        IdsListDto ids = new IdsListDto();
        ids.setIds(List.of(1));
        assertThatThrownBy(() -> controller.validationAdministrativeMultiple(ids))
                .isInstanceOf(AppException.class);

        connecte("ges1", Role.GES);
        IdsListDto vide = new IdsListDto();
        vide.setIds(List.of());
        assertThatThrownBy(() -> controller.validationAdministrativeMultiple(vide))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("liste est vide");
    }

    @Test
    void updateDUneConventionValideeNotifieSelonLeRole() {
        Utilisateur etudiant = connecte("etu1", Role.ETU);
        Convention convention = new Convention();
        convention.setValidationCreation(true);
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        stubSetConventionData();

        controller.update(42, new ConventionFormDto());
        verify(mailerService).sendValidationMail(eq(convention), any(), eq(etudiant),
                eq(TemplateMail.CODE_ETU_MODIF_CONVENTION), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

        Utilisateur gestionnaire = connecte("ges1", Role.GES);
        controller.update(42, new ConventionFormDto());
        verify(mailerService).sendValidationMail(eq(convention), any(), eq(gestionnaire),
                eq(TemplateMail.CODE_GES_MODIF_CONVENTION), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    // ------------------------------------------------------------------
    // validation de création
    // ------------------------------------------------------------------

    private Convention conventionDEtudiant(String identEtudiant, boolean autoriserChevauchement) {
        Convention convention = conventionValidable();
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant(identEtudiant);
        convention.setEtudiant(etudiant);
        convention.getCentreGestion().setAutoriserChevauchement(autoriserChevauchement);
        return convention;
    }

    @Test
    void laValidationDeCreationNotifieSelonLeRole() {
        Utilisateur etudiant = connecte("etu1", Role.ETU);
        Convention convention = conventionDEtudiant("etu1", true);

        controller.validationCreation(42);

        assertThat(convention.isValidationCreation()).isTrue();
        assertThat(convention.getDateValidationCreation()).isNotNull();
        verify(mailerService).sendValidationMail(eq(convention), any(), eq(etudiant),
                eq(TemplateMail.CODE_ETU_CREA_CONVENTION), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

        Utilisateur gestionnaire = connecte("ges1", Role.GES);
        controller.validationCreation(42);
        verify(mailerService).sendValidationMail(eq(convention), any(), eq(gestionnaire),
                eq(TemplateMail.CODE_GES_CREA_CONVENTION), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> controller.validationCreation(42)).isInstanceOf(AppException.class);
        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.validationCreation(99)).isInstanceOf(AppException.class);
    }

    @Test
    void laValidationDeCreationRefuseLeChevauchementDeDates() {
        connecte("etu1", Role.ETU);
        Convention convention = conventionDEtudiant("etu1", false);
        convention.setDateDebutStage(new Date(1700000000000L));
        convention.setDateFinStage(new Date(1700100000000L));
        when(conventionJpaRepository.findDatesChevauchent(eq("etu1"), eq(42), any(), any()))
                .thenReturn(List.of(1));

        assertThatThrownBy(() -> controller.validationCreation(42))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("chevauchent");
    }

    // ------------------------------------------------------------------
    // singleFieldUpdate : mise à jour champ par champ
    // ------------------------------------------------------------------

    private Convention conventionModifiable() {
        Convention convention = conventionDEtudiant("etu1", true);
        when(conventionService.isConventionModifiable(eq(convention), any())).thenReturn(true);
        return convention;
    }

    private Convention patch(String field, Object value) {
        ConventionSingleFieldDto dto = new ConventionSingleFieldDto();
        dto.setField(field);
        dto.setValue(value);
        return controller.singleFieldUpdate(42, dto);
    }

    @Test
    void singleFieldUpdateModifieLesChampsSimples() {
        connecte("ges1", Role.GES);
        Convention convention = conventionModifiable();

        patch("sujetStage", "Sujet");
        patch("competences", "Java");
        patch("fonctionsEtTaches", "Dév");
        patch("details", "Détails");
        patch("modeEncadreSuivi", "Hebdo");
        patch("avantagesNature", "Repas");
        patch("travailNuitFerie", "Non");
        patch("commentaireDureeTravail", "35h");
        patch("montantGratification", "600");
        patch("informationsComplementaires", "Note libre");
        patch("interruptionStage", Boolean.TRUE);
        patch("horairesReguliers", Boolean.TRUE);
        patch("gratificationStage", Boolean.TRUE);
        patch("confidentiel", Boolean.FALSE);
        patch("protectionSocialeOrganismeAccueil", Boolean.TRUE);
        patch("accordAnnuaireEtudiant", Boolean.TRUE);
        patch("nbHeuresHebdo", 35);
        patch("nbConges", 2);
        patch("dateDebutStage", "2026-02-01T00:00:00Z");
        patch("dateFinStage", "2026-06-30T00:00:00Z");

        assertThat(convention.getSujetStage()).isEqualTo("Sujet");
        assertThat(convention.getCompetences()).isEqualTo("Java");
        assertThat(convention.getFonctionsEtTaches()).isEqualTo("Dév");
        assertThat(convention.getDetails()).isEqualTo("Détails");
        assertThat(convention.getModeEncadreSuivi()).isEqualTo("Hebdo");
        assertThat(convention.getAvantagesNature()).isEqualTo("Repas");
        assertThat(convention.getTravailNuitFerie()).isEqualTo("Non");
        assertThat(convention.getCommentaireDureeTravail()).isEqualTo("35h");
        assertThat(convention.getMontantGratification()).isEqualTo("600");
        assertThat(convention.getCommentaireStage()).isEqualTo("Note libre");
        assertThat(convention.getInterruptionStage()).isTrue();
        assertThat(convention.getHorairesReguliers()).isTrue();
        assertThat(convention.getGratificationStage()).isTrue();
        assertThat(convention.getConfidentiel()).isFalse();
        assertThat(convention.getProtectionSocialeOrganismeAccueil()).isTrue();
        assertThat(convention.getAccordAnnuaireEtudiant()).isTrue();
        assertThat(convention.getNbHeuresHebdo()).isEqualTo("35");
        assertThat(convention.getNbConges()).isEqualTo("2");
        assertThat(convention.getDateDebutStage()).isEqualTo(Date.from(Instant.parse("2026-02-01T00:00:00Z")));
        assertThat(convention.getDateFinStage()).isEqualTo(Date.from(Instant.parse("2026-06-30T00:00:00Z")));
    }

    @Test
    void updateAccordAnnuaireResteAutoriseSurUneConventionNonModifiable() {
        connecte("etu1", Role.ETU);
        Convention convention = conventionDEtudiant("etu1", true);
        // La convention n'est plus modifiable : le PATCH générique serait refusé,
        // mais le retrait de l'accord annuaire doit rester possible.
        when(conventionService.isConventionModifiable(eq(convention), any())).thenReturn(false);

        AccordAnnuaireDto dto = new AccordAnnuaireDto();
        dto.setAccordAnnuaireEtudiant(Boolean.FALSE);
        controller.updateAccordAnnuaire(42, dto);

        assertThat(convention.getAccordAnnuaireEtudiant()).isFalse();
        verify(conventionService).canViewEditConvention(eq(convention), any(), eq(DroitEnum.MODIFICATION));
        verify(conventionJpaRepository).saveAndFlush(convention);
    }

    @Test
    void updateAccordAnnuaireRefuseUneConventionInconnue() {
        connecte("etu1", Role.ETU);
        when(conventionJpaRepository.findById(42)).thenReturn(null);

        AccordAnnuaireDto dto = new AccordAnnuaireDto();
        dto.setAccordAnnuaireEtudiant(Boolean.TRUE);

        assertThatThrownBy(() -> controller.updateAccordAnnuaire(42, dto))
                .isInstanceOf(AppException.class);
    }

    @Test
    void singleFieldUpdateResoutLesNomenclatures() {
        connecte("ges1", Role.GES);
        Convention convention = conventionModifiable();

        LangueConvention langue = new LangueConvention();
        langue.setCode("EN");
        langue.setLibelle("Anglais");
        when(langueConventionJpaRepository.findByCode("EN")).thenReturn(langue);
        when(paysJpaRepository.findById(5)).thenReturn(new Pays());
        TypeConvention typeConvention = new TypeConvention();
        typeConvention.setLibelle("Stage");
        when(typeConventionJpaRepository.findById(5)).thenReturn(typeConvention);
        when(themeJpaRepository.findById(5)).thenReturn(new Theme());
        when(tempsTravailJpaRepository.findById(5)).thenReturn(new TempsTravail());
        when(uniteGratificationJpaRepository.findById(5)).thenReturn(new UniteGratification());
        when(uniteDureeJpaRepository.findById(5)).thenReturn(new UniteDuree());
        when(deviseJpaRepository.findById(5)).thenReturn(new Devise());
        when(modeVersGratificationJpaRepository.findById(5)).thenReturn(new ModeVersGratification());
        when(origineStageJpaRepository.findById(5)).thenReturn(new OrigineStage());
        when(natureTravailJpaRepository.findById(5)).thenReturn(new NatureTravail());
        when(modeValidationStageJpaRepository.findById(5)).thenReturn(new ModeValidationStage());

        patch("codeLangueConvention", "EN");
        patch("idPays", 5);
        patch("idTypeConvention", 5);
        patch("idTheme", 5);
        patch("idTempsTravail", 5);
        patch("idUniteGratification", 5);
        patch("idUniteDuree", 5);
        patch("idDevise", 5);
        patch("idModeVersGratification", 5);
        patch("idOrigineStage", 5);
        patch("idNatureTravail", 5);
        patch("idModeValidationStage", 5);

        assertThat(convention.getLangueConvention().getCode()).isEqualTo("EN");
        assertThat(convention.getPaysConvention()).isNotNull();
        assertThat(convention.getTheme()).isNotNull();
        assertThat(convention.getTempsTravail()).isNotNull();
        assertThat(convention.getUniteGratification()).isNotNull();
        assertThat(convention.getUniteDureeGratification()).isNotNull();
        assertThat(convention.getDevise()).isNotNull();
        assertThat(convention.getModeVersGratification()).isNotNull();
        assertThat(convention.getOrigineStage()).isNotNull();
        assertThat(convention.getNatureTravail()).isNotNull();
        assertThat(convention.getModeValidationStage()).isNotNull();

        when(paysJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> patch("idPays", 99))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void singleFieldUpdateCascadeLeChangementDeStructureEtService() {
        connecte("ges1", Role.GES);
        Convention convention = conventionModifiable();
        convention.setService(new Service());
        convention.setContact(new Contact());
        convention.setSignataire(new Contact());

        Structure structure = new Structure();
        structure.setId(5);
        when(structureJpaRepository.findById(5)).thenReturn(structure);
        patch("idStructure", 5);
        assertThat(convention.getStructure()).isSameAs(structure);
        assertThat(convention.getService()).isNull();
        assertThat(convention.getContact()).isNull();
        assertThat(convention.getSignataire()).isNull();

        Service service = new Service();
        service.setId(5);
        when(serviceJpaRepository.findById(5)).thenReturn(service);
        patch("idService", 5);
        assertThat(convention.getService()).isSameAs(service);

        when(contactJpaRepository.findById(5)).thenReturn(new Contact());
        patch("idContact", 5);
        patch("idSignataire", 5);
        assertThat(convention.getContact()).isNotNull();
        assertThat(convention.getSignataire()).isNotNull();

        when(enseignantJpaRepository.findById(5)).thenReturn(new Enseignant());
        patch("idEnseignant", 5);
        assertThat(convention.getEnseignant()).isNotNull();

        ConventionSingleFieldDto duree = new ConventionSingleFieldDto();
        duree.setField("dureeExceptionnelle");
        duree.setValue(12);
        duree.setDureeExceptionnellePeriode("P1;P2");
        controller.singleFieldUpdate(42, duree);
        assertThat(convention.getDureeExceptionnelle()).isEqualTo("12");
        assertThat(convention.getDureeExceptionnellePeriode()).isEqualTo("P1;P2");
    }

    @Test
    void singleFieldUpdateControleLesEntrees() {
        connecte("ges1", Role.GES);
        Convention convention = conventionModifiable();

        assertThatThrownBy(() -> patch("informationsComplementaires", "a".repeat(1001)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("limité à 1000 caractères");
        assertThatThrownBy(() -> patch("informationsComplementaires", 12))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Valeur invalide");

        when(conventionService.isConventionModifiable(eq(convention), any())).thenReturn(false);
        assertThatThrownBy(() -> patch("sujetStage", "Sujet"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("n'est plus modifiable");

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        ConventionSingleFieldDto dto = new ConventionSingleFieldDto();
        dto.setField("sujetStage");
        dto.setValue("x");
        assertThatThrownBy(() -> controller.singleFieldUpdate(99, dto)).isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // conventions en attente
    // ------------------------------------------------------------------

    @Test
    void leComptageDesConventionsEnAttenteSuitLOrdreDeValidation() {
        connecte("ens1", Role.ENS);
        Convention sansCentre = new Convention();
        Convention avecCentre = new Convention();
        CentreGestion centre = new CentreGestion();
        centre.setValidationPedagogiqueOrdre(2);
        centre.setVerificationAdministrativeOrdre(1);
        centre.setValidationConventionOrdre(1);
        avecCentre.setCentreGestion(centre);
        avecCentre.setVerificationAdministrative(true);
        avecCentre.setValidationConvention(true);
        when(conventionJpaRepository.getConventionEnAttenteEnseignant("2025/2026", "ens1"))
                .thenReturn(List.of(sansCentre, avecCentre));

        assertThat(controller.countConventionEnAttente("2025")).isZero();

        connecte("ges1", Role.GES);
        when(conventionJpaRepository.getConventionEnAttenteGestionnaire("2025/2026", "ges1")).thenReturn(List.of());
        assertThat(controller.countConventionEnAttente("2025")).isZero();

        connecte("adm1", Role.ADM);
        when(conventionJpaRepository.getConventionEnAttenteGestionnaire("2025/2026")).thenReturn(List.of());
        assertThat(controller.countConventionEnAttente("2025")).isZero();
    }

    // ------------------------------------------------------------------
    // suppression
    // ------------------------------------------------------------------

    @Test
    void deleteSupprimeUneConventionSansValidation() {
        connecte("ges1", Role.GES);
        Convention convention = conventionDEtudiant("etu1", true);
        convention.getCentreGestion().setValidationConvention(false);
        convention.getCentreGestion().setValidationPedagogique(false);
        convention.getCentreGestion().setVerificationAdministrative(false);
        convention.setValidationConvention(false);
        convention.setValidationPedagogique(false);
        convention.setVerificationAdministrative(false);

        assertThat(controller.delete(42)).isSameAs(convention);
        verify(conventionDocumentEtudiantService).deleteAllForConvention(convention);
        verify(conventionJpaRepository).delete(convention);
    }

    @Test
    void deleteRefuseUneConventionValidee() {
        connecte("ges1", Role.GES);
        Convention convention = conventionDEtudiant("etu1", true);
        convention.getCentreGestion().setValidationConvention(true);
        convention.getCentreGestion().setValidationPedagogique(false);
        convention.getCentreGestion().setVerificationAdministrative(false);
        convention.setValidationConvention(true);
        convention.setValidationPedagogique(false);
        convention.setVerificationAdministrative(false);

        assertThatThrownBy(() -> controller.delete(42))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("déjà été validée");

        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> controller.delete(42)).isInstanceOf(AppException.class);
        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
    }

    @Test
    void deleteBrouillonNettoieLeBrouillonSIlExiste() {
        connecte("etu1", Role.ETU);
        Convention brouillon = new Convention();
        when(conventionJpaRepository.findBrouillon("etu1")).thenReturn(brouillon);

        controller.deleteBrouillon();
        verify(conventionDocumentEtudiantService).deleteAllForConvention(brouillon);
        verify(conventionJpaRepository).delete(brouillon);

        when(conventionJpaRepository.findBrouillon("etu1")).thenReturn(null);
        controller.deleteBrouillon();
        verify(conventionJpaRepository, org.mockito.Mockito.times(1)).delete(any(Convention.class));
    }

    // ------------------------------------------------------------------
    // PDF
    // ------------------------------------------------------------------

    @Test
    void lePdfConventionCompleteLEtablissementDeReference() {
        connecte("ges1", Role.GES);
        Convention convention = conventionDEtudiant("etu1", true);
        CentreGestion etablissement = new CentreGestion();
        etablissement.setNomCentre("Université de Lorraine");
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(etablissement);

        ResponseEntity<byte[]> pdf = controller.getConventionPDF(42, false);

        assertThat(pdf.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(convention.getNomEtabRef()).isEqualTo("Université de Lorraine");
        verify(impressionService).generateConventionAvenantPDF(eq(convention), eq(null), any(), eq(false));

        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(null);
        convention.setNomEtabRef(null);
        convention.setAdresseEtabRef(null);
        assertThatThrownBy(() -> controller.getConventionPDF(42, false))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("établissement");

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getConventionPDF(99, false)).isInstanceOf(AppException.class);
    }

    @Test
    void lePdfAvenantExigeUnAvenantExistant() {
        connecte("ges1", Role.GES);
        Convention convention = conventionDEtudiant("etu1", true);
        Avenant avenant = new Avenant();
        avenant.setConvention(convention);
        when(avenantJpaRepository.findById(7)).thenReturn(avenant);

        assertThat(controller.getAvenantPDF(7).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(impressionService).generateConventionAvenantPDF(eq(convention), eq(avenant), any(), eq(false));

        when(avenantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getAvenantPDF(99)).isInstanceOf(AppException.class);
    }

    @Test
    void lApercuPdfDelegueALImpression() {
        connecte("ges1", Role.GES);
        assertThat(controller.generateConventionPreview(3, 7).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(impressionService).generatePreviewPDF(eq(3), any(), eq(7));
    }

    // ------------------------------------------------------------------
    // signature électronique
    // ------------------------------------------------------------------

    @Test
    void lEnvoiEnSignatureMarqueLExpediteur() {
        connecte("ges1", Role.GES);
        Convention convention = conventionDEtudiant("etu1", true);
        when(conventionJpaRepository.findById((Integer) 42)).thenReturn(Optional.of(convention));
        IdsListDto ids = new IdsListDto();
        ids.setIds(List.of(42));
        when(signatureService.upload(ids, false)).thenReturn(1);

        assertThat(controller.envoiSignatureElectroniqueMultiple(ids)).isEqualTo(1);
        assertThat(convention.getLoginEnvoiSignature()).isEqualTo("ges1");
        verify(conventionJpaRepository).save(convention);
    }

    @Test
    void leControleDeSignatureVerifieLaConfiguration() {
        connecte("ges1", Role.GES);
        ConfigGeneraleDto configDesactivee = mock(ConfigGeneraleDto.class);
        when(configDesactivee.isSignatureEnabled()).thenReturn(false);
        when(appConfigService.getConfigGenerale()).thenReturn(configDesactivee);

        assertThatThrownBy(() -> controller.controleSignatureElectronique(42))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("n'est pas configurée");

        ConfigGeneraleDto configActive = mock(ConfigGeneraleDto.class);
        when(configActive.isSignatureEnabled()).thenReturn(true);
        when(appConfigService.getConfigGenerale()).thenReturn(configActive);
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.DOCAPOSTE);

        Convention convention = conventionDEtudiant("etu1", true);
        convention.getCentreGestion().setNomCentre("Centre A");
        assertThatThrownBy(() -> controller.controleSignatureElectronique(42))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("circuit de signature");

        convention.getCentreGestion().setCircuitSignature("WF-1");
        ResponseDto reponse = new ResponseDto();
        when(conventionService.controleEmailTelephone(convention)).thenReturn(reponse);
        assertThat(controller.controleSignatureElectronique(42)).isSameAs(reponse);

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.controleSignatureElectronique(99)).isInstanceOf(AppException.class);
    }

    @Test
    void laMiseAJourDesInfosDeSignatureDelegueAuService() {
        connecte("ges1", Role.GES);
        ConfigGeneraleDto configActive = mock(ConfigGeneraleDto.class);
        when(configActive.isSignatureEnabled()).thenReturn(true);
        when(appConfigService.getConfigGenerale()).thenReturn(configActive);
        Convention convention = conventionDEtudiant("etu1", true);

        assertThat(controller.updateSignatureElectroniqueInfo(42)).isSameAs(convention);
        verify(signatureService).update(convention);

        ConfigGeneraleDto configDesactivee = mock(ConfigGeneraleDto.class);
        when(configDesactivee.isSignatureEnabled()).thenReturn(false);
        when(appConfigService.getConfigGenerale()).thenReturn(configDesactivee);
        assertThatThrownBy(() -> controller.updateSignatureElectroniqueInfo(42)).isInstanceOf(AppException.class);
    }

    @Test
    void leTelechargementDuDocumentSigneLitLeFichier() throws Exception {
        connecte("ges1", Role.GES);
        Convention convention = conventionDEtudiant("etu1", true);
        MetadataDto metadata = mock(MetadataDto.class);
        when(metadata.getTitle()).thenReturn("doc-signe");
        when(signatureService.getPublicMetadata(convention)).thenReturn(metadata);

        Path fichier = tempDir.resolve("doc-signe.pdf");
        Files.write(fichier, "PDF-SIGNE".getBytes());
        when(signatureService.getSignatureFilePath("doc-signe")).thenReturn(fichier.toString());
        assertThat(controller.downloadDoc(42).getBody()).isEqualTo("PDF-SIGNE".getBytes());

        when(signatureService.getSignatureFilePath("doc-signe")).thenReturn(tempDir.resolve("absent.pdf").toString());
        assertThatThrownBy(() -> controller.downloadDoc(42))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Fichier non trouvé");
    }

    // ------------------------------------------------------------------
    // chevauchement, périodes, historique, enseignant référent
    // ------------------------------------------------------------------

    @Test
    void leControleDeChevauchementNeConcerneQueLesEtudiants() {
        connecte("ges1", Role.GES);
        DateStageDto dates = new DateStageDto();
        assertThat(controller.isChevauchement(42, dates)).isFalse();

        connecte("etu1", Role.ETU);
        Convention convention = conventionDEtudiant("etu1", true);
        dates.setDateDebut(new Date(1700000000000L));
        dates.setDateFin(new Date(1700100000000L));
        when(conventionJpaRepository.findDatesChevauchent(eq("etu1"), eq(42), any(), any()))
                .thenReturn(List.of(1));
        assertThat(controller.isChevauchement(42, dates)).isTrue();

        dates.setDateFin(null);
        assertThat(controller.isChevauchement(42, dates)).isFalse();

        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> controller.isChevauchement(42, dates)).isInstanceOf(AppException.class);
    }

    @Test
    void lesPeriodesEtLHistoriqueSontAccessibles() {
        connecte("ges1", Role.GES);
        Convention convention = conventionDEtudiant("etu1", true);

        PeriodesDto periodes = new PeriodesDto();
        periodes.setPeriodes("P1;P2");
        assertThat(controller.updatePeriodes(42, periodes).getDureeExceptionnellePeriode()).isEqualTo("P1;P2");

        when(historiqueValidationJpaRepository.findByConvention(42)).thenReturn(List.of(new HistoriqueValidation()));
        assertThat(controller.getHistoriqueValidations(42)).hasSize(1);

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.updatePeriodes(99, periodes)).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.getHistoriqueValidations(99)).isInstanceOf(AppException.class);
    }

    @Test
    void leChangementDEnseignantReferentNotifieLesGestionnaires() {
        Utilisateur gestionnaire = connecte("ges1", Role.GES);
        Convention convention = conventionDEtudiant("etu1", true);
        convention.setValidationCreation(true);
        PersonnelCentreGestion personnel = new PersonnelCentreGestion();
        personnel.setMail("centre@univ.fr");
        convention.getCentreGestion().setPersonnels(List.of(personnel));
        when(mailerService.isAlerteActif(personnel, "CHANGEMENT_ENS")).thenReturn(true);

        Enseignant enseignant = new Enseignant();
        enseignant.setMail("prof@univ.fr");
        when(enseignantJpaRepository.findById((Integer) 5)).thenReturn(Optional.of(enseignant));

        Convention resultat = controller.changeEnseignantReferent(42, Map.of("idEnseignant", 5));

        assertThat(resultat.getEnseignant()).isSameAs(enseignant);
        verify(mailerService).sendAlerteValidation("centre@univ.fr", convention, null, gestionnaire, "CHANGEMENT_ENS");

        when(enseignantJpaRepository.findById((Integer) 99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> controller.changeEnseignantReferent(42, Map.of("idEnseignant", 99)))
                .isInstanceOf(AppException.class);
        when(conventionJpaRepository.findById(77)).thenReturn(null);
        assertThatThrownBy(() -> controller.changeEnseignantReferent(77, Map.of("idEnseignant", 5)))
                .isInstanceOf(AppException.class);
    }
}
