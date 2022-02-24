package org.esup_portail.esup_stage.security.filter;

import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.exception.ApplicationClientException;
import org.esup_portail.esup_stage.model.Etudiant;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.EtudiantJpaRepository;
import org.esup_portail.esup_stage.repository.PersonnelCentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.RoleJpaRepository;
import org.esup_portail.esup_stage.repository.UtilisateurJpaRepository;
import org.esup_portail.esup_stage.security.common.CasLayer;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CasFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CasFilter.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Autowired
    UtilisateurJpaRepository utilisateurJpaRepository;

    @Autowired
    RoleJpaRepository roleJpaRepository;

    @Autowired
    PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;

    @Autowired
    EtudiantJpaRepository etudiantJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    LdapService ldapService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // URL D'appel du service
        String	requestURL	=httpServletRequest.getRequestURL().toString();
        String	port		=httpServletRequest.getServerPort()+"";
        String	protocol	=httpServletRequest.getHeader("x-forwarded-proto");
        logger.debug("port " + port +", protocol : "+protocol);

        if (httpServletRequest.getSession(true).getAttribute("casUser") == null && httpServletRequest.getSession().getAttribute("tokenUser") == null) {
            String newURL = requestURL;
            String jSessionId = null;
            Cookie[] cookies = httpServletRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("JSESSIONID")) {
                        jSessionId = cookie.getValue();
                    }
                }
            }
            logger.debug(" casSession : " + jSessionId);

            String requestUrl = httpServletRequest.getRequestURL().toString();
            logger.debug("requestUrl : " + requestUrl);

            String ticket = httpServletRequest.getParameter("ticket");
            logger.debug("ticket : " + ticket);

            if (ticket == null || ticket.length() == 0) {
                // absence de ticket => aller vers le CAS
                String redirectCas = applicationBootstrap.getAppConfig().getCasUrlLogin().replace("{service}", requestURL);
                logger.debug("Ticket non trouvé, redirection vers cas : " + redirectCas);
                httpServletResponse.sendRedirect(redirectCas);
            } else {
                // retour d'un ticket analyse
                CasLayer.CasUser casUser = null;
                CasLayer cas = new CasLayer(applicationBootstrap.getAppConfig().getCasUrlService());
                try {
                    casUser = cas.getCasUser(ticket, requestUrl);
                } catch (ApplicationClientException e) {
                    logger.warn("login non trouvé");
                }

                if (casUser != null && casUser.getLogin() !=null) {
                    httpServletRequest.getSession(true).setAttribute("casUser", casUser);
                    //casifié ?
                    httpServletRequest.getSession(false).setAttribute("isUserSession", true);
                    logger.debug("login : " + casUser.getLogin());
                } else {
                    logger.debug("login vide");
                    HttpServletResponse httpResp = (HttpServletResponse) response;
                    httpResp.sendError(HttpServletResponse.SC_FORBIDDEN, "Non autorisé");
                    return;
                }

                // Recherche de l'utilisateur
                Utilisateur utilisateur = utilisateurJpaRepository.findOneByLogin(casUser.getLogin());
                LdapSearchDto ldapSearchDto = new LdapSearchDto();
                ldapSearchDto.setSupannAliasLogin(casUser.getLogin());
                List<LdapUser> users = ldapService.search("/etudiant", ldapSearchDto);

                // création de l'utilisateur avec le rôle correspondant (ETU, ENS : à rechercher dans le LDAP) s'il n'existe pas en base
                if (utilisateur == null) {
                    String role = Role.ETU;
                    if (users.size() == 0) {
                        String filter = appConfigService.getConfigGenerale().getLdapFiltreEnseignant() + "(&(supannAliasLogin=" + casUser.getLogin() + "))";
                        users = ldapService.searchByFilter(filter);
                        role = Role.ENS;
                    }

                    if (users.size() == 1) {
                        // Création de l'utilisateur
                        List<Role> roles = new ArrayList<>();
                        roles.add(roleJpaRepository.findOneByCode(role));
                        // si l'enseignant est rattaché à un centre de gestion, on lui ajoute le rôle gestionnaire
                        if (role.equals(Role.ENS)) {
                            long count = personnelCentreGestionJpaRepository.countPersonnelByLogin(casUser.getLogin());
                            if (count > 0) {
                                roles.add(roleJpaRepository.findOneByCode(Role.GES));
                            }
                        }
                        utilisateur = new Utilisateur();
                        utilisateur.setLogin(casUser.getLogin());
                        utilisateur.setNom(casUser.getNom());
                        utilisateur.setPrenom(casUser.getPrenom());
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
                            etudiant.setIdentEtudiant(casUser.getLogin());
                            etudiant.setNumEtudiant(users.get(0).getCodEtu());
                            etudiant.setNom(casUser.getNom());
                            etudiant.setPrenom(casUser.getPrenom());
                            etudiant.setMail(users.get(0).getMail());
                            etudiant.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
                            etudiant.setLoginCreation(casUser.getLogin());
                            etudiantJpaRepository.saveAndFlush(etudiant);
                        }
                    }

                    boolean update = false;
                    if (utilisateur.getNom() == null) {
                        utilisateur.setNom(casUser.getNom());
                        update = true;
                    }
                    if (utilisateur.getPrenom() == null) {
                        utilisateur.setPrenom(casUser.getPrenom());
                        update = true;
                    }
                    if (update) {
                        utilisateurJpaRepository.saveAndFlush(utilisateur);
                    }
                }

                // log de redirection
                logger.debug("tomcat rewrite '" + requestURL + "' -> '" + newURL + "'");
                httpServletResponse.sendRedirect(newURL);
            }
        } else {
            httpServletResponse.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
            chain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
