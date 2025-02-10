package org.esup_portail.esup_stage.security.userdetails;

import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.model.Etudiant;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private EtudiantRepository etudiantRepository;

    @Autowired
    private LdapService ldapService;

    @Autowired
    private AppConfigService appConfigService;

    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken authentication) throws UsernameNotFoundException {
<<<<<<< HEAD
=======
        AttributePrincipal principal = authentication.getAssertion().getPrincipal();
>>>>>>> feature/dockerisation
        String username = authentication.getName();

        // Recherche de l'utilisateur
        Utilisateur utilisateur = utilisateurJpaRepository.findOneByLogin(username);
        LdapSearchDto ldapSearchDto = new LdapSearchDto();
        ldapSearchDto.setSupannAliasLogin(username);

        // création de l'utilisateur avec le rôle correspondant (ETU, ENS : à rechercher dans le LDAP) s'il n'existe pas en base
        if (utilisateur == null) {
            List<LdapUser> users = ldapService.search("/etudiant", ldapSearchDto);
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
                    long count = personnelCentreGestionJpaRepository.countPersonnelByLogin(users.get(0).getUid());
                    if (count > 0) {
                        roles.add(roleJpaRepository.findOneByCode(Role.GES));
                    }
                }
                utilisateur = new Utilisateur();
                utilisateur.setLogin(username);
                utilisateur.setNom(String.join(" ", users.get(0).getSn()));
                utilisateur.setPrenom(String.join(" ", users.get(0).getGivenName()));
                utilisateur.setActif(true);
                utilisateur.setRoles(roles);
                utilisateur.setUid(users.get(0).getUid());
                utilisateur = utilisateurJpaRepository.saveAndFlush(utilisateur);
            }
        }

        // éventuel mise à jour de son nom/prénom si non existant
        if (utilisateur != null) {
            LdapUser ldapUser = ldapService.searchByLogin(utilisateur.getLogin());
            if (ldapUser == null) {
                throw new UsernameNotFoundException("Utilisateur LDAP non trouvé à partir du login " + utilisateur.getLogin());
            }
            String nom = String.join(" ", ldapUser.getSn());
            String prenom = String.join(" ", ldapUser.getGivenName());
            // Si c'est un étudiant on lui créé une ligne dans la table Etudiant s'il n'existe pas
            if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                Etudiant etudiant = etudiantRepository.findByNumEtudiant(ldapUser.getCodEtu());
                if (etudiant == null) {
                    etudiant = new Etudiant();
                    etudiant.setIdentEtudiant(ldapUser.getUid());
                    etudiant.setNumEtudiant(ldapUser.getCodEtu());
                    etudiant.setNom(nom);
                    etudiant.setPrenom(prenom);
                    etudiant.setMail(ldapUser.getMail());
                    etudiant.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
                    etudiant.setLoginCreation(username);
                    etudiantJpaRepository.saveAndFlush(etudiant);
                } else if (etudiant.getMail().isEmpty()) {
                    etudiant.setMail(ldapUser.getMail());
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
            if (utilisateur.getUid() == null) {
                utilisateur.setUid(ldapUser.getUid());
                update = true;
            }
            if (UtilisateurHelper.isRole(utilisateur, Role.ETU) && utilisateur.getNumEtudiant() == null) {
                utilisateur.setNumEtudiant(ldapUser.getCodEtu());
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
