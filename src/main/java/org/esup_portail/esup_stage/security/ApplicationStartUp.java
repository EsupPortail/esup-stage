package org.esup_portail.esup_stage.security;

import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.enums.FolderEnum;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.RoleJpaRepository;
import org.esup_portail.esup_stage.repository.UtilisateurJpaRepository;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
@Slf4j
public class ApplicationStartUp {

    @Autowired
    private UtilisateurJpaRepository utilisateurRepository;

    @Autowired
    private RoleJpaRepository roleJpaRepository;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private LdapService ldapService;

    @Autowired
    private AppliProperties appliProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void createAdminTechs() {
        for (String username : appliProperties.getAdminTechnique()) {
            createAdminTech(username);
        }
    }

    public void createAdminTech(final String username) {
        if (StringUtils.hasText(username)) {
            Role roleAdmTech = roleJpaRepository.findOneByCode(Role.ADM);
            log.info("Utilisateur admin technique ADM depuis la config : {}", username);
            Utilisateur utilisateur = utilisateurRepository.findOneByLogin(username);
            if (utilisateur == null) {
                utilisateur = new Utilisateur();
            }
            utilisateur.setLogin(username);
            utilisateur.setActif(true);
            if (!utilisateur.getRoles().contains(roleAdmTech)) {
                utilisateur.getRoles().add(roleAdmTech);
            }
            utilisateurRepository.saveAndFlush(utilisateur);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadTheming() throws IOException, URISyntaxException {
        log.info("Initialisation du thème de l'application");
        appConfigService.updateTheme();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initUid() {
        log.info("Récupération de l'uid des utilisateurs déjà existants");
        List<Utilisateur> utilisateurs = utilisateurRepository.findNoUid();
        log.info("{} utilisateurs sans uid - recherche dans le LDAP", utilisateurs.size());
        for (Utilisateur utilisateur : utilisateurs) {
            LdapUser ldapUser = ldapService.searchByLogin(utilisateur.getLogin());
            if (ldapUser != null) {
                utilisateur.setUid(ldapUser.getUid());
                utilisateurRepository.save(utilisateur);
            }
        }
        log.info("Mise à jour des uid terminé");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initFolders() throws IOException {
        log.info("Création des dossiers dans le répertoire upload si non existant");
        for (FolderEnum folderEnum : FolderEnum.values()) {
            String path = appliProperties.getDataDir() + folderEnum;
            File dir = new File(path);
            if (!dir.isDirectory()) {
                log.info("Création du dossier {}", path);
                Files.createDirectories(Paths.get(path));
            }
        }
        log.info("Création des dossiers terminé");
    }
}
