package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeStageJpaRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConventionControllerValidationTest {

    @InjectMocks
    ConventionController conventionController;

    @Mock
    ConventionJpaRepository conventionJpaRepository;

    @Mock
    PeriodeStageJpaRepository periodeStageJpaRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validationCreation_rejectsIrregularHoursWithoutPeriods() {
        Convention convention = new Convention();
        convention.setId(1);
        convention.setHorairesReguliers(false);

        Utilisateur utilisateur = new Utilisateur();
        Role role = new Role();
        role.setCode(Role.GES);
        utilisateur.setRoles(List.of(role));

        CasUserDetailsImpl userDetails = new CasUserDetailsImpl(utilisateur, List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of())
        );

        when(conventionJpaRepository.findById(1)).thenReturn(convention);
        when(periodeStageJpaRepository.findByConvention(convention)).thenReturn(Collections.emptyList());

        AppException exception = assertThrows(AppException.class, () -> conventionController.validationCreation(1));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains("période de travail");
    }
}
