package org.esup_portail.esup_stage.security.userdetails;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.model.Etudiant;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.esup_portail.esup_stage.service.proprety.ConfigMissingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CasUserDetailsServiceImplTest {

    private CasUserDetailsServiceImpl service;
    private UtilisateurJpaRepository utilisateurJpaRepository;
    private RoleJpaRepository roleJpaRepository;
    private PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;
    private EtudiantJpaRepository etudiantJpaRepository;
    private EtudiantRepository etudiantRepository;
    private LdapService ldapService;
    private AppConfigService appConfigService;
    private ConfigMissingService configMissingService;
    private AppliProperties appliProperties;

    @BeforeEach
    void setUp() {
        service = new CasUserDetailsServiceImpl();
        utilisateurJpaRepository = mock(UtilisateurJpaRepository.class);
        roleJpaRepository = mock(RoleJpaRepository.class);
        personnelCentreGestionJpaRepository = mock(PersonnelCentreGestionJpaRepository.class);
        etudiantJpaRepository = mock(EtudiantJpaRepository.class);
        etudiantRepository = mock(EtudiantRepository.class);
        ldapService = mock(LdapService.class);
        appConfigService = mock(AppConfigService.class);
        configMissingService = mock(ConfigMissingService.class);
        appliProperties = mock(AppliProperties.class);
        ReflectionTestUtils.setField(service, "utilisateurJpaRepository", utilisateurJpaRepository);
        ReflectionTestUtils.setField(service, "roleJpaRepository", roleJpaRepository);
        ReflectionTestUtils.setField(service, "personnelCentreGestionJpaRepository", personnelCentreGestionJpaRepository);
        ReflectionTestUtils.setField(service, "etudiantJpaRepository", etudiantJpaRepository);
        ReflectionTestUtils.setField(service, "etudiantRepository", etudiantRepository);
        ReflectionTestUtils.setField(service, "ldapService", ldapService);
        ReflectionTestUtils.setField(service, "appConfigService", appConfigService);
        ReflectionTestUtils.setField(service, "configMissingService", configMissingService);
        ReflectionTestUtils.setField(service, "appliProperties", appliProperties);

        when(utilisateurJpaRepository.saveAndFlush(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));
        when(configMissingService.hasMissingKeys()).thenReturn(false);
    }

    private CasAssertionAuthenticationToken authentification(String login) {
        CasAssertionAuthenticationToken token = mock(CasAssertionAuthenticationToken.class);
        when(token.getName()).thenReturn(login);
        return token;
    }

    private Role role(String code) {
        Role role = new Role();
        role.setCode(code);
        return role;
    }

    private LdapUser ldapUser(String uid) {
        LdapUser ldapUser = mock(LdapUser.class);
        when(ldapUser.getUid()).thenReturn(uid);
        when(ldapUser.getSn()).thenReturn(List.of("Durand"));
        when(ldapUser.getGivenName()).thenReturn(List.of("Alice"));
        when(ldapUser.getCodEtu()).thenReturn("123");
        when(ldapUser.getMail()).thenReturn("alice@univ.fr");
        return ldapUser;
    }

    @Test
    void lAdminTechniqueSeConnecteMemeAvecUneConfigIncomplete() {
        when(configMissingService.hasMissingKeys()).thenReturn(true);
        when(appliProperties.isAdminTechnique("admin")).thenReturn(true);
        Role admin = role(Role.ADM);
        when(roleJpaRepository.findOneByCode(Role.ADM)).thenReturn(admin);
        Utilisateur existant = new Utilisateur();
        existant.setLogin("admin");
        existant.setRoles(new ArrayList<>());
        when(utilisateurJpaRepository.findOneByLogin("admin")).thenReturn(existant);

        UserDetails details = service.loadUserDetails(authentification("admin"));

        assertThat(existant.getRoles()).contains(admin);
        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority).contains(Role.ADM);
    }

    @Test
    void unUtilisateurExistantEstCompleteDepuisLeLdap() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin("etu1");
        utilisateur.setRoles(List.of(role(Role.ETU)));
        when(utilisateurJpaRepository.findOneByLogin("etu1")).thenReturn(utilisateur);
        LdapUser ldapUser = ldapUser("uid-etu1");
        when(ldapService.searchByLogin("etu1")).thenReturn(ldapUser);
        when(etudiantRepository.findByNumEtudiant("123")).thenReturn(null);
        ConfigGeneraleDto config = mock(ConfigGeneraleDto.class);
        when(config.getCodeUniversite()).thenReturn("UL");
        when(appConfigService.getConfigGenerale()).thenReturn(config);

        UserDetails details = service.loadUserDetails(authentification("etu1"));

        assertThat(utilisateur.getNom()).isEqualTo("Durand");
        assertThat(utilisateur.getPrenom()).isEqualTo("Alice");
        assertThat(utilisateur.getUid()).isEqualTo("uid-etu1");
        assertThat(utilisateur.getNumEtudiant()).isEqualTo("123");
        ArgumentCaptor<Etudiant> etudiant = ArgumentCaptor.forClass(Etudiant.class);
        verify(etudiantJpaRepository).saveAndFlush(etudiant.capture());
        assertThat(etudiant.getValue().getCodeUniversite()).isEqualTo("UL");
        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority).contains(Role.ETU);
    }

    @Test
    void unNouvelEtudiantEstCreeDepuisLeLdap() {
        when(utilisateurJpaRepository.findOneByLogin("etu2")).thenReturn(null);
        LdapUser ldapUser = ldapUser("uid-etu2");
        when(ldapService.search(eq("/etudiant"), any(LdapSearchDto.class))).thenReturn(List.of(ldapUser));
        when(roleJpaRepository.findOneByCode(Role.ETU)).thenReturn(role(Role.ETU));
        when(ldapService.searchByLogin("etu2")).thenReturn(ldapUser);
        Etudiant etudiant = new Etudiant();
        etudiant.setMail("");
        when(etudiantRepository.findByNumEtudiant("123")).thenReturn(etudiant);

        UserDetails details = service.loadUserDetails(authentification("etu2"));

        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority).contains(Role.ETU);
        assertThat(etudiant.getMail()).isEqualTo("alice@univ.fr");
    }

    @Test
    void unEnseignantRattacheAUnCentreDevientAussiGestionnaire() {
        when(utilisateurJpaRepository.findOneByLogin("ens1")).thenReturn(null);
        LdapUser ldapUser = ldapUser("uid-ens1");
        when(ldapService.search(eq("/etudiant"), any(LdapSearchDto.class))).thenReturn(List.of());
        when(ldapService.search(eq("/tuteur"), any(LdapSearchDto.class))).thenReturn(List.of(ldapUser));
        when(roleJpaRepository.findOneByCode(Role.ENS)).thenReturn(role(Role.ENS));
        when(roleJpaRepository.findOneByCode(Role.GES)).thenReturn(role(Role.GES));
        when(personnelCentreGestionJpaRepository.countPersonnelByLogin("uid-ens1")).thenReturn(1L);
        when(ldapService.searchByLogin("ens1")).thenReturn(ldapUser);

        UserDetails details = service.loadUserDetails(authentification("ens1"));

        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .contains(Role.ENS, Role.GES);
    }

    @Test
    void unMembreDuPersonnelEstCreeSansRole() {
        when(utilisateurJpaRepository.findOneByLogin("staff1")).thenReturn(null);
        LdapUser ldapUser = ldapUser("uid-staff1");
        when(ldapService.search(eq("/etudiant"), any(LdapSearchDto.class))).thenReturn(List.of());
        when(ldapService.search(eq("/tuteur"), any(LdapSearchDto.class))).thenReturn(List.of());
        when(ldapService.search(eq("/staff"), any(LdapSearchDto.class))).thenReturn(List.of(ldapUser));
        when(ldapService.searchByLogin("staff1")).thenReturn(ldapUser);

        UserDetails details = service.loadUserDetails(authentification("staff1"));

        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    void unInconnuDuLdapEstRefuse() {
        when(utilisateurJpaRepository.findOneByLogin("inconnu")).thenReturn(null);
        when(ldapService.search(any(), any(LdapSearchDto.class))).thenReturn(List.of());

        assertThatThrownBy(() -> service.loadUserDetails(authentification("inconnu")))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void unUtilisateurSansFicheLdapEstRefuse() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin("fantome");
        utilisateur.setRoles(List.of());
        when(utilisateurJpaRepository.findOneByLogin("fantome")).thenReturn(utilisateur);
        when(ldapService.searchByLogin("fantome")).thenReturn(null);

        assertThatThrownBy(() -> service.loadUserDetails(authentification("fantome")))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("LDAP");
    }
}
