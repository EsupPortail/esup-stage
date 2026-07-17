package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.AvenantResponseDto;
import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.MailerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvenantControllerTest {

    private AvenantController controller;
    private AvenantJpaRepository avenantJpaRepository;
    private ConventionJpaRepository conventionJpaRepository;
    private AppConfigService appConfigService;
    private MailerService mailerService;

    @BeforeEach
    void setUp() {
        controller = new AvenantController();
        avenantJpaRepository = mock(AvenantJpaRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        appConfigService = mock(AppConfigService.class);
        mailerService = mock(MailerService.class);
        controller.avenantJpaRepository = avenantJpaRepository;
        controller.conventionJpaRepository = conventionJpaRepository;
        controller.appConfigService = appConfigService;
        controller.mailerService = mailerService;

        when(appConfigService.getConfigAlerteMail()).thenReturn(new ConfigAlerteMailDto());
        when(avenantJpaRepository.save(any(Avenant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(avenantJpaRepository.saveAndFlush(any(Avenant.class))).thenAnswer(inv -> inv.getArgument(0));
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

    private Avenant avenantDe(String identEtudiant) {
        Avenant avenant = new Avenant();
        avenant.setId(9);
        Convention convention = new Convention();
        convention.setId(42);
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant(identEtudiant);
        convention.setEtudiant(etudiant);
        avenant.setConvention(convention);
        return avenant;
    }

    @Test
    void getByIdControleLAccesEtudiant() {
        Avenant avenant = avenantDe("etu1");
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);

        connecte("etu1", Role.ETU);
        assertThat(controller.getById(9)).isNotNull();

        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> controller.getById(9)).isInstanceOf(AppException.class);

        when(avenantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getById(99)).isInstanceOf(AppException.class);
    }

    @Test
    void getByConventionFiltreLesEtudiants() {
        connecte("etu1", Role.ETU);
        Convention convention = new Convention();
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant("etu1");
        convention.setEtudiant(etudiant);
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        when(avenantJpaRepository.findByConvention(42)).thenReturn(List.of(avenantDe("etu1")));

        List<AvenantResponseDto> avenants = controller.getByConvention(42);

        assertThat(avenants).hasSize(1);

        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> controller.getByConvention(42)).isInstanceOf(AppException.class);
    }

    @Test
    void validateBasculeLeFlagEtNotifie() {
        connecte("ges1", Role.GES);
        Avenant avenant = avenantDe("etu1");
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);

        AvenantResponseDto dto = controller.validate(9);

        assertThat(avenant.isValidationAvenant()).isTrue();
        assertThat(avenant.getDateValidation()).isNotNull();
        assertThat(dto).isNotNull();
        verify(mailerService).sendValidationMail(eq(avenant.getConvention()), eq(avenant), any(Utilisateur.class),
                eq(TemplateMail.CODE_AVENANT_VALIDATION), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

        when(avenantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.validate(99)).isInstanceOf(AppException.class);
    }

    @Test
    void cancelValidationRemetLAvenantEnAttente() {
        connecte("ges1", Role.GES);
        Avenant avenant = avenantDe("etu1");
        avenant.setValidationAvenant(true);
        avenant.setDateValidation(new java.util.Date());
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);

        controller.cancelValidation(9);

        assertThat(avenant.isValidationAvenant()).isFalse();
        assertThat(avenant.getDateValidation()).isNull();

        when(avenantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.cancelValidation(99)).isInstanceOf(AppException.class);
    }
}
