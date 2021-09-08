package fr.dauphine.estage.security.filter;

import fr.dauphine.estage.bootstrap.ApplicationBootstrap;
import fr.dauphine.estage.exception.ApplicationClientException;
import fr.dauphine.estage.model.Utilisateur;
import fr.dauphine.estage.repository.UtilisateurJpaRepository;
import fr.dauphine.estage.security.common.CasLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CasFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CasFilter.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Autowired
    UtilisateurJpaRepository utilisateurRepository;

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

                // Recherche de l'utilisateur en base pour mettre à jour son nom/prénom si non existant
                Utilisateur utilisateur = utilisateurRepository.findOneByLogin(casUser.getLogin());
                if (utilisateur != null) {
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
                        utilisateurRepository.saveAndFlush(utilisateur);
                    }
                }

                // log de redirection
                logger.debug("tomcat rewrite '" + requestURL + "' -> '" + newURL + "'");
                httpServletResponse.sendRedirect(newURL);
            }
        } else {
            chain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
