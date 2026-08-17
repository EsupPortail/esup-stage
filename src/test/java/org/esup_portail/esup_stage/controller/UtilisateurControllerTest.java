package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.UtilisateurDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.RoleJpaRepository;
import org.esup_portail.esup_stage.repository.UtilisateurJpaRepository;
import org.esup_portail.esup_stage.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UtilisateurControllerTest {

    private UtilisateurController controller;
    private UtilisateurRepository utilisateurRepository;
    private UtilisateurJpaRepository utilisateurJpaRepository;
    private RoleJpaRepository roleJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new UtilisateurController();
        utilisateurRepository = mock(UtilisateurRepository.class);
        utilisateurJpaRepository = mock(UtilisateurJpaRepository.class);
        roleJpaRepository = mock(RoleJpaRepository.class);
        AppliProperties appliProperties = new AppliProperties();
        appliProperties.setAdminTechnique("admintech1;admintech2");
        controller.utilisateurRepository = utilisateurRepository;
        controller.utilisateurJpaRepository = utilisateurJpaRepository;
        controller.roleJpaRepository = roleJpaRepository;
        controller.appliProperties = appliProperties;

        when(utilisateurJpaRepository.saveAndFlush(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Utilisateur utilisateur(String login) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(login);
        utilisateur.setUid(login);
        utilisateur.setNom("Nom");
        utilisateur.setPrenom("Prenom");
        return utilisateur;
    }

    @Test
    void lUtilisateurConnecteEstRenvoyeSansCache() {
        when(utilisateurJpaRepository.findOneByLogin("alice")).thenReturn(utilisateur("alice"));
        var authentication = new UsernamePasswordAuthenticationToken(
                new User("alice", "", List.of()), null);

        var reponse = controller.getUserConnected(authentication);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reponse.getHeaders().getPragma()).isEqualTo("no-cache");
        assertThat(reponse.getBody().getLogin()).isEqualTo("alice");
    }

    @Test
    void sansAuthentificationLaRequeteEstRejetee() {
        assertThatThrownBy(() -> controller.getUserConnected(null))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void rechercheEtExportsDeleguentAuRepository() {
        when(utilisateurRepository.count(anyString())).thenReturn(1L);
        when(utilisateurRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(List.of(utilisateur("alice")));
        when(utilisateurRepository.exportExcel(any(), any(), any(), any())).thenReturn("xls".getBytes());
        when(utilisateurRepository.exportCsv(any(), any(), any(), any())).thenReturn(new StringBuilder("csv"));

        PaginatedResponse<UtilisateurDto> resultat =
                controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());
        assertThat(resultat.getTotal()).isEqualTo(1L);
        assertThat(resultat.getData()).hasSize(1);

        assertThat(controller.exportExcel("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody()).isNotEmpty();
        assertThat(controller.exportCsv("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody()).isEqualTo("csv");
    }

    @Test
    void updateRemplaceRolesEtEtat() {
        Utilisateur existant = utilisateur("alice");
        when(utilisateurJpaRepository.findById(7)).thenReturn(existant);
        Role roleGes = new Role();
        roleGes.setId(2);
        when(roleJpaRepository.findById(2)).thenReturn(roleGes);

        Utilisateur demande = utilisateur("alice");
        demande.setNom("Nouveau");
        Role roleDemande = new Role();
        roleDemande.setId(2);
        demande.setRoles(List.of(roleDemande));
        demande.setActif(true);

        UtilisateurDto dto = controller.update(7, demande);

        assertThat(dto.getNom()).isEqualTo("Nouveau");
        assertThat(existant.getRoles()).containsExactly(roleGes);
        assertThat(existant.getActif()).isTrue();

        when(utilisateurJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.update(99, demande)).isInstanceOf(AppException.class);
    }

    @Test
    void createRefuseLesDoublonsEtDesactiveParDefaut() {
        when(utilisateurJpaRepository.findOneByLogin("alice")).thenReturn(utilisateur("alice"));
        assertThatThrownBy(() -> controller.create(utilisateur("alice")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("deja existant");

        when(utilisateurJpaRepository.findOneByLogin("bob")).thenReturn(null);
        Utilisateur nouveau = utilisateur("bob");
        nouveau.setRoles(List.of());

        UtilisateurDto dto = controller.create(nouveau);

        assertThat(dto.getLogin()).isEqualTo("bob");
        assertThat(nouveau.getActif()).isFalse();
    }

    @Test
    void laPersonneEstProjeteeDepuisSonLogin() {
        when(utilisateurJpaRepository.findOneByLogin("alice")).thenReturn(utilisateur("alice"));

        var personne = controller.getPersonneByLogin("alice");

        assertThat(personne.getNom()).isEqualTo("Nom");
        assertThat(personne.getPrenom()).isEqualTo("Prenom");

        when(utilisateurJpaRepository.findOneByLogin("inconnu")).thenReturn(null);
        assertThatThrownBy(() -> controller.getPersonneByLogin("inconnu")).isInstanceOf(AppException.class);
    }

    @Test
    void lesAdminsTechniquesSontListes() {
        assertThat(controller.getAdminTech()).containsExactlyInAnyOrder("admintech1", "admintech2");
    }
}
