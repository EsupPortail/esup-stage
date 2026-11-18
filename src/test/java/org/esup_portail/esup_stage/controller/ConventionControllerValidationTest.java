package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.HistoriqueValidation;
import org.esup_portail.esup_stage.model.LangueConvention;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.TypeConvention;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.HistoriqueValidationJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeStageJpaRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConventionService;
import org.esup_portail.esup_stage.service.MailerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConventionControllerValidationTest {

    @InjectMocks
    ConventionController conventionController;

    @Mock
    ConventionJpaRepository conventionJpaRepository;

    @Mock
    PeriodeStageJpaRepository periodeStageJpaRepository;

    @Mock
    ConventionService conventionService;

    @Mock
    AppConfigService appConfigService;

    @Mock
    HistoriqueValidationJpaRepository historiqueValidationJpaRepository;

    @Mock
    MailerService mailerService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validationCreation_rejectsIrregularHoursWithoutPeriods() {
        Convention convention = new Convention();
        convention.setId(1);
        convention.setHorairesReguliers(false);

        connecte(utilisateur(Role.GES));

        when(conventionJpaRepository.findById(1)).thenReturn(convention);
        when(periodeStageJpaRepository.findByConvention(convention)).thenReturn(Collections.emptyList());

        AppException exception = assertThrows(AppException.class, () -> conventionController.validationCreation(1));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains("période de travail");
    }

    @Test
    void validate_refuseLaValidationAdministrativePourUnEnseignantSeul() {
        Convention convention = new Convention();
        convention.setId(42);
        connecte(utilisateur(Role.ENS));
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        doNothing().when(conventionService).canViewEditConvention(eq(convention), any(Utilisateur.class));
        when(conventionService.isEnseignantSansGestionnaire(any(Utilisateur.class))).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> conventionController.validate(42, "validationConvention"));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains("Type de validation inconnu");
    }

    @Test
    void validate_autoriseLaValidationAdministrativePourUnGestionnaireEnseignant() {
        Convention convention = conventionPourValidationAdmin(42);
        connecte(utilisateur(Role.GES, Role.ENS));
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        doNothing().when(conventionService).canViewEditConvention(eq(convention), any(Utilisateur.class));
        when(conventionService.isEnseignantSansGestionnaire(any(Utilisateur.class))).thenReturn(false);
        when(appConfigService.getConfigAlerteMail()).thenReturn(new ConfigAlerteMailDto());
        when(conventionJpaRepository.saveAndFlush(any(Convention.class))).thenAnswer(inv -> inv.getArgument(0));
        when(historiqueValidationJpaRepository.saveAndFlush(any(HistoriqueValidation.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(conventionService).validationAutoDonnees(any(Convention.class), any(Utilisateur.class));

        assertThatCode(() -> conventionController.validate(42, "validationConvention"))
                .doesNotThrowAnyException();

        assertThat(convention.getValidationConvention()).isTrue();
        verify(mailerService).sendValidationMail(any(Convention.class), isNull(), any(Utilisateur.class),
                anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    private Convention conventionPourValidationAdmin(int id) {
        Convention convention = new Convention();
        convention.setId(id);
        LangueConvention langue = new LangueConvention();
        langue.setLibelle("français");
        convention.setLangueConvention(langue);
        TypeConvention typeConvention = new TypeConvention();
        typeConvention.setLibelle("Stage");
        convention.setTypeConvention(typeConvention);
        return convention;
    }

    private void connecte(Utilisateur utilisateur) {
        CasUserDetailsImpl userDetails = new CasUserDetailsImpl(utilisateur, List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of())
        );
    }

    private Utilisateur utilisateur(String... roleCodes) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid("user1");
        utilisateur.setLogin("user1");
        utilisateur.setRoles(java.util.Arrays.stream(roleCodes).map(code -> {
            Role role = new Role();
            role.setCode(code);
            return role;
        }).toList());
        return utilisateur;
    }

}
