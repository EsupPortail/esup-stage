package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConventionFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.LangueConvention;
import org.esup_portail.esup_stage.model.Role;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConventionControllerBrouillonTest {

    private static final String LOGIN = "monian23";
    private static final String MSG_DOUBLON = "Vous avez déjà une convention en cours de création. Veuillez rafraichir la page.";

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
        Role role = new Role();
        role.setCode(Role.GES);
        utilisateur.setRoles(List.of(role));
        CasUserDetailsImpl userDetails = new CasUserDetailsImpl(utilisateur, List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void stubSetConventionDataMinimal() {
        doAnswer(invocation -> {
            Convention convention = invocation.getArgument(0);
            LangueConvention langue = new LangueConvention();
            langue.setLibelle("français");
            convention.setLangueConvention(langue);
            TypeConvention type = new TypeConvention();
            type.setLibelle("Stage");
            convention.setTypeConvention(type);
            return null;
        }).when(conventionService).setConventionData(any(Convention.class), any(ConventionFormDto.class));
    }

    @Test
    void getBrouillon_sansBrouillon_retourneConventionVide() {
        when(conventionJpaRepository.findBrouillon(LOGIN)).thenReturn(null);

        Convention result = conventionController.getBrouillon();

        assertThat(result.getId()).isZero();
    }

    @Test
    void getBrouillon_avecUnBrouillon_retourneCeBrouillon() {
        Convention brouillon = new Convention();
        brouillon.setId(66902);
        when(conventionJpaRepository.findBrouillon(LOGIN)).thenReturn(brouillon);

        Convention result = conventionController.getBrouillon();

        assertThat(result.getId()).isEqualTo(66902);
    }

    @Test
    void create_siBrouillonExistant_retourneBadRequest() {
        Convention existing = new Convention();
        existing.setId(1);
        when(conventionJpaRepository.findBrouillon(LOGIN)).thenReturn(existing);

        AppException ex = assertThrows(AppException.class,
                () -> conventionController.create(new ConventionFormDto()));

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).isEqualTo(MSG_DOUBLON);
        verify(conventionJpaRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_siViolationIntegrite_retourneBadRequest() {
        stubSetConventionDataMinimal();
        when(conventionJpaRepository.findBrouillon(LOGIN)).thenReturn(null);
        doThrow(new DataIntegrityViolationException("uk_convention_brouillon_login"))
                .when(conventionJpaRepository).saveAndFlush(any(Convention.class));

        AppException ex = assertThrows(AppException.class,
                () -> conventionController.create(new ConventionFormDto()));

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).isEqualTo(MSG_DOUBLON);
    }

    @Test
    void create_sansBrouillon_persiste() {
        stubSetConventionDataMinimal();
        when(conventionJpaRepository.findBrouillon(LOGIN)).thenReturn(null);
        Convention saved = new Convention();
        saved.setId(42);
        when(conventionJpaRepository.saveAndFlush(any(Convention.class))).thenReturn(saved);

        Convention result = conventionController.create(new ConventionFormDto());

        assertThat(result.getId()).isEqualTo(42);
        verify(conventionService).setConventionData(any(Convention.class), any(ConventionFormDto.class));
    }

    @Test
    void deleteBrouillon_supprimeSiPresent() {
        Convention brouillon = new Convention();
        brouillon.setId(10);
        when(conventionJpaRepository.findBrouillon(LOGIN)).thenReturn(brouillon);

        conventionController.deleteBrouillon();

        verify(conventionJpaRepository).delete(brouillon);
    }

    @Test
    void findBrouillon_default_prendLePlusRecentEtNullSiVide() {
        ConventionJpaRepository repo = Mockito.mock(ConventionJpaRepository.class, Mockito.CALLS_REAL_METHODS);
        Convention newer = new Convention();
        newer.setId(66902);
        Convention older = new Convention();
        older.setId(66901);
        when(repo.findBrouillons(LOGIN)).thenReturn(List.of(newer, older));
        when(repo.findBrouillons("inconnu")).thenReturn(Collections.emptyList());

        assertThat(repo.findBrouillon(LOGIN).getId()).isEqualTo(66902);
        assertThat(repo.findBrouillon("inconnu")).isNull();
    }
}
