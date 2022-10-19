package org.esup_portail.esup_stage.security.userdetails;

import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.model.Etudiant;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.EtudiantJpaRepository;
import org.esup_portail.esup_stage.repository.PersonnelCentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.RoleJpaRepository;
import org.esup_portail.esup_stage.repository.UtilisateurJpaRepository;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

public class CasUserDetailsServiceImpl implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

    @Autowired
    private UtilisateurJpaRepository utilisateurJpaRepository;

    @Autowired
    private RoleJpaRepository roleJpaRepository;

    @Autowired
    private PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;

    @Autowired
    private EtudiantJpaRepository etudiantJpaRepository;

    @Autowired
    private LdapService ldapService;

    @Autowired
    private AppConfigService appConfigService;

    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken authentication) throws UsernameNotFoundException {
        AttributePrincipal principal = authentication.getAssertion().getPrincipal();
        String username = authentication.getName();
        Map<String, Object> attributes = principal.getAttributes();
        String nom = getStringValue(attributes.get("sn"));
        String prenom = getStringValue(attributes.get("givenName"));

        // Recherche de l'utilisateur
        Utilisateur utilisateur = utilisateurJpaRepository.findOneByLogin(username);
        LdapSearchDto ldapSearchDto = new LdapSearchDto();
        ldapSearchDto.setSupannAliasLogin(username);
        List<LdapUser> users = ldapService.search("/etudiant", ldapSearchDto);

        // création de l'utilisateur avec le rôle correspondant (ETU, ENS : à rechercher dans le LDAP) s'il n'existe pas en base
        if (utilisateur == null) {
            String role = Role.ETU;
            if (users.size() == 0) {
                users = ldapService.search("/tuteur", ldapSearchDto);
                role = Role.ENS;
            }

            if (users.size() == 1) {
                // Création de l'utilisateur
                List<Role> roles = new ArrayList<>();
                roles.add(roleJpaRepository.findOneByCode(role));
                // si l'enseignant est rattaché à un centre de gestion, on lui ajoute le rôle gestionnaire
                if (role.equals(Role.ENS)) {
                    long count = personnelCentreGestionJpaRepository.countPersonnelByLogin(username);
                    if (count > 0) {
                        roles.add(roleJpaRepository.findOneByCode(Role.GES));
                    }
                }
                utilisateur = new Utilisateur();
                utilisateur.setLogin(username);
                utilisateur.setNom(nom);
                utilisateur.setPrenom(prenom);
                utilisateur.setActif(true);
                utilisateur.setRoles(roles);
                utilisateur = utilisateurJpaRepository.saveAndFlush(utilisateur);
            }
        }

        // éventuel mise à jour de son nom/prénom si non existant
        if (utilisateur != null) {
            // Si c'est un étudiant on lui créé une ligne dans la table Etudiant s'il n'existe pas
            if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                Etudiant etudiant = etudiantJpaRepository.findByNumEtudiant(users.get(0).getCodEtu());
                if (etudiant == null) {
                    etudiant = new Etudiant();
                    etudiant.setIdentEtudiant(username);
                    etudiant.setNumEtudiant(users.get(0).getCodEtu());
                    etudiant.setNom(nom);
                    etudiant.setPrenom(prenom);
                    etudiant.setMail(users.get(0).getMail());
                    etudiant.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
                    etudiant.setLoginCreation(username);
                    etudiantJpaRepository.saveAndFlush(etudiant);
                }
            }

            boolean update = false;
            if (utilisateur.getNom() == null) {
                utilisateur.setNom(nom);
                update = true;
            }
            if (utilisateur.getPrenom() == null) {
                utilisateur.setPrenom(prenom);
                update = true;
            }
            if (update) {
                utilisateurJpaRepository.saveAndFlush(utilisateur);
            }
        }

        if (utilisateur == null) {
            throw new UsernameNotFoundException("Utilisateur non trouvé");
        }

        return new CasUserDetailsImpl(utilisateur, getGrantedAuthorities(utilisateur));
    }

    private Set<GrantedAuthority> getGrantedAuthorities(Utilisateur utilisateur) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (utilisateur.getRoles() != null) {
            for (Role role : utilisateur.getRoles()) {
                authorities.add(new SimpleGrantedAuthority(role.getCode()));
            }
        }
        return authorities;
    }

    private String getStringValue(Object obj) {
        String value = null;
        if (obj != null) {
            if (obj instanceof String)
                value = (String) obj;
            else if (obj instanceof List && ((List<?>) obj).size() > 0 && ((List<?>) obj).get(0) instanceof String)
                value = (String) ((List<?>) obj).get(0);

        }
        return value;
    }
}
