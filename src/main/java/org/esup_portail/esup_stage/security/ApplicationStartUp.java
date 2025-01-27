package org.esup_portail.esup_stage.security;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ApplicationStartUp {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartUp.class);

    @Autowired
    UtilisateurJpaRepository utilisateurRepository;

    @Autowired
    RoleJpaRepository roleJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    LdapService ldapService;

    @Value("${appli.admin_technique}")
    private String adminTechs;

    @Value("${appli.data_dir}")
    private String dataDir;

    @EventListener(ApplicationReadyEvent.class)
    public void createAdminTech() {
        Role roleAdmTech = roleJpaRepository.findOneByCode(Role.ADM);
        for (String login : (new String[]{adminTechs})) {
            Utilisateur utilisateur = utilisateurRepository.findOneByLogin(login);
            if (utilisateur == null) {
                logger.info("Création utilisateur ADM_TECH " + login);
                utilisateur = new Utilisateur();
                utilisateur.setLogin(login);
                utilisateur.setActif(true);
                utilisateur.getRoles().add(roleAdmTech);
                utilisateurRepository.saveAndFlush(utilisateur);
            }
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadTheming() throws IOException, URISyntaxException {
        logger.info("Initialisation du thème de l'application");
        appConfigService.updateTheme();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initUid() {
        logger.info("Récupération de l'uid des utilisateurs déjà existants");
        List<Utilisateur> utilisateurs = utilisateurRepository.findNoUid();
        logger.info(utilisateurs.size() + " utilisateurs sans uid - recherche dans le LDAP");
        for (Utilisateur utilisateur : utilisateurs) {
            LdapUser ldapUser = ldapService.searchByLogin(utilisateur.getLogin());
            if (ldapUser != null) {
                utilisateur.setUid(ldapUser.getUid());
                utilisateurRepository.save(utilisateur);
            }
        }
        logger.info("Mise à jour des uid terminé");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initFolders() throws IOException {
        logger.info("Création des dossiers dans le répertoire upload si non existant");
        for (FolderEnum folderEnum : FolderEnum.values()) {
            String path = dataDir + folderEnum;
            File dir = new File(path);
            if (!dir.isDirectory()) {
                logger.info("Création du dossier " + path);
                Files.createDirectories(Paths.get(path));
            }
        }
        logger.info("Création des dossiers terminé");
    }
}
