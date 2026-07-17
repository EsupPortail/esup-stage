package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.AnneeUniversitaireDto;
import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ConventionFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConventionService;
import org.esup_portail.esup_stage.service.MailerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConventionControllerTest {

    private ConventionController controller;
    private ConventionJpaRepository conventionJpaRepository;
    private AppConfigService appConfigService;
    private ConventionService conventionService;
    private MailerService mailerService;

    @BeforeEach
    void setUp() {
        controller = new ConventionController();
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        appConfigService = mock(AppConfigService.class);
        conventionService = mock(ConventionService.class);
        mailerService = mock(MailerService.class);
        controller.conventionJpaRepository = conventionJpaRepository;
        controller.appConfigService = appConfigService;
        controller.conventionService = conventionService;
        controller.mailerService = mailerService;

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
        when(conventionJpaRepository.getGestionnaireAnnees("ges1")).thenReturn(List.of("2025/2026"));
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
        controller.historiqueValidationJpaRepository =
                mock(org.esup_portail.esup_stage.repository.HistoriqueValidationJpaRepository.class);
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

        verify(conventionService, org.mockito.Mockito.times(4)).canViewEditConvention(eq(convention), any());
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
        controller.historiqueValidationJpaRepository =
                mock(org.esup_portail.esup_stage.repository.HistoriqueValidationJpaRepository.class);
        connecte("ges1", Role.GES);

        Convention eligible = conventionValidable();
        eligible.setValidationPedagogique(true);

        org.esup_portail.esup_stage.dto.IdsListDto ids = new org.esup_portail.esup_stage.dto.IdsListDto();
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

        org.esup_portail.esup_stage.dto.IdsListDto ids = new org.esup_portail.esup_stage.dto.IdsListDto();
        ids.setIds(List.of(42));

        var reponse = controller.validationAdministrativeMultiple(ids);

        assertThat(reponse.getBody().get("message")).contains("n'a pas pu être validée administrativement");
    }

    @Test
    void laValidationMultipleControleLesEntrees() {
        connecte("ens1", Role.ENS);
        org.esup_portail.esup_stage.dto.IdsListDto ids = new org.esup_portail.esup_stage.dto.IdsListDto();
        ids.setIds(List.of(1));
        assertThatThrownBy(() -> controller.validationAdministrativeMultiple(ids))
                .isInstanceOf(AppException.class);

        connecte("ges1", Role.GES);
        org.esup_portail.esup_stage.dto.IdsListDto vide = new org.esup_portail.esup_stage.dto.IdsListDto();
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
}
