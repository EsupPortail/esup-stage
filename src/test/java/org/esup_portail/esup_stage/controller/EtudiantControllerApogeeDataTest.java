package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.EtudiantJpaRepository;
import org.esup_portail.esup_stage.repository.EtudiantRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.EtudiantSecurityService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EtudiantControllerApogeeDataTest {

    private EtudiantController controller;
    private ApogeeService apogeeService;
    private EtudiantSecurityService etudiantSecurityService;

    @BeforeEach
    void setUp() {
        controller = new EtudiantController();
        apogeeService = mock(ApogeeService.class);
        AppConfigService appConfigService = mock(AppConfigService.class);
        etudiantSecurityService = mock(EtudiantSecurityService.class);

        controller.apogeeService = apogeeService;
        controller.appConfigService = appConfigService;
        controller.etudiantRepository = mock(EtudiantRepository.class);
        controller.etudiantJpaRepository = mock(EtudiantJpaRepository.class);
        controller.etudiantSecurityService = etudiantSecurityService;

        when(appConfigService.getAnneeUniv()).thenReturn("2026");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void traduitUn404DuControleDePerimetreEnEtudiantNonTrouve() {
        authenticate(utilisateurAvecRole(Role.GES));
        when(etudiantSecurityService.isGestionnaireOrResponsableGestionnaire(any())).thenReturn(true);
        when(etudiantSecurityService.isEtuInCentreGestionUtilisateur(any(), eq("123")))
                .thenThrow(new AppException(HttpStatus.NOT_FOUND, "Aucune donnée trouvée"));

        assertThatThrownBy(() -> controller.getApogeeData("123"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> {
                    AppException exception = (AppException) e;
                    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(exception.getMessage()).isEqualTo("Étudiant non trouvé");
                });
    }

    @Test
    void traduitUn404DeGetInfoApogeeEnEtudiantNonTrouve() {
        authenticate(utilisateurAvecRole(Role.ADM));
        when(etudiantSecurityService.isGestionnaireOrResponsableGestionnaire(any())).thenReturn(false);
        when(apogeeService.getInfoApogee("123", "2026"))
                .thenThrow(new AppException(HttpStatus.NOT_FOUND, "Aucune donnée trouvée"));

        assertThatThrownBy(() -> controller.getApogeeData("123"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> {
                    AppException exception = (AppException) e;
                    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(exception.getMessage()).isEqualTo("Étudiant non trouvé");
                });
    }

    @Test
    void conserveLe403QuandLEtudiantEstHorsPerimetre() {
        authenticate(utilisateurAvecRole(Role.GES));
        when(etudiantSecurityService.isGestionnaireOrResponsableGestionnaire(any())).thenReturn(true);
        when(etudiantSecurityService.isEtuInCentreGestionUtilisateur(any(), eq("123"))).thenReturn(false);

        assertThatThrownBy(() -> controller.getApogeeData("123"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getHttpStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void propageUneErreurTechnique() {
        authenticate(utilisateurAvecRole(Role.ADM));
        when(etudiantSecurityService.isGestionnaireOrResponsableGestionnaire(any())).thenReturn(false);
        when(apogeeService.getInfoApogee("123", "2026"))
                .thenThrow(new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue."));

        assertThatThrownBy(() -> controller.getApogeeData("123"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getHttpStatus())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Utilisateur utilisateurAvecRole(String codeRole) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid("uid-test");
        utilisateur.setLogin("test");
        Role role = new Role();
        role.setCode(codeRole);
        utilisateur.setRoles(List.of(role));
        return utilisateur;
    }

    private void authenticate(Utilisateur utilisateur) {
        CasUserDetailsImpl principal = new CasUserDetailsImpl(utilisateur, List.of());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
