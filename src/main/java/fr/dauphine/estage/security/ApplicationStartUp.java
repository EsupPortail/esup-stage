package fr.dauphine.estage.security;

import fr.dauphine.estage.bootstrap.ApplicationBootstrap;
import fr.dauphine.estage.model.Role;
import fr.dauphine.estage.model.RoleEnum;
import fr.dauphine.estage.model.Utilisateur;
import fr.dauphine.estage.repository.RoleRepository;
import fr.dauphine.estage.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartUp {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartUp.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Autowired
    UtilisateurRepository utilisateurRepository;

    @Autowired
    RoleRepository roleRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void createAdminTech() {
        Role roleAdmTech = roleRepository.findOneByCode(RoleEnum.ADM_TECH);
        for (String login : applicationBootstrap.getAppConfig().getAdminTechs()) {
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
}
