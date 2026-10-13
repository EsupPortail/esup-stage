package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConventionFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.LangueConvention;
import org.esup_portail.esup_stage.model.TypeConvention;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.ConventionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConventionControllerCreateTest {

    private static final String LOGIN = "etu1";

    @InjectMocks
    ConventionController conventionController;

    @Mock
    ConventionJpaRepository conventionJpaRepository;

    @Mock
    ConventionService conventionService;

    @BeforeEach
    void setUp() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(LOGIN);
        CasUserDetailsImpl userDetails = new CasUserDetailsImpl(utilisateur, List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of())
        );
        // Données minimales requises par Convention.setValeurNomenclature()
        lenient().doAnswer(invocation -> {
            Convention convention = invocation.getArgument(0);
            convention.setLangueConvention(new LangueConvention());
            convention.setTypeConvention(new TypeConvention());
            return null;
        }).when(conventionService).setConventionData(any(Convention.class), any(ConventionFormDto.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_savesBrouillonWhenNoneExists() {
        Convention saved = new Convention();
        when(conventionJpaRepository.findBrouillon(LOGIN)).thenReturn(null);
        when(conventionJpaRepository.saveAndFlush(any(Convention.class))).thenReturn(saved);

        Convention result = conventionController.create(new ConventionFormDto());

        assertSame(saved, result);
    }

    @Test
    void create_rejectsWhenBrouillonAlreadyExists() {
        when(conventionJpaRepository.findBrouillon(LOGIN)).thenReturn(new Convention());

        AppException exception = assertThrows(AppException.class, () -> conventionController.create(new ConventionFormDto()));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains("convention en cours de création");
        verify(conventionJpaRepository, never()).saveAndFlush(any(Convention.class));
    }

    @Test
    void create_convertsUniqueViolationFromConcurrentCreationIntoUserMessage() {
        // Le premier contrôle ne voit rien, la requête concurrente insère entre-temps son brouillon
        when(conventionJpaRepository.findBrouillon(LOGIN)).thenReturn(null, new Convention());
        when(conventionJpaRepository.saveAndFlush(any(Convention.class)))
                .thenThrow(new DataIntegrityViolationException("uk_convention_brouillon_login"));

        AppException exception = assertThrows(AppException.class, () -> conventionController.create(new ConventionFormDto()));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains("convention en cours de création");
    }

    @Test
    void create_rethrowsIntegrityViolationNotRelatedToBrouillon() {
        DataIntegrityViolationException violation = new DataIntegrityViolationException("autre contrainte");
        when(conventionJpaRepository.findBrouillon(LOGIN)).thenReturn(null);
        when(conventionJpaRepository.saveAndFlush(any(Convention.class))).thenThrow(violation);

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> conventionController.create(new ConventionFormDto()));

        assertSame(violation, exception);
    }
}
