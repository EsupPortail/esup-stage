package fr.dauphine.estage.security.filter;

import fr.dauphine.estage.bootstrap.ApplicationBootstrap;
import fr.dauphine.estage.model.Utilisateur;
import fr.dauphine.estage.repository.UtilisateurJpaRepository;
import fr.dauphine.estage.security.TokenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TokenFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TokenFilter.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Autowired
    UtilisateurJpaRepository utilisateurRepository;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        logger.info("TokenFilter.doFilter");
        String[] pathInfos = req.getRequestURI().split("/");
        if (pathInfos.length==4) {
            // l'utilisateur se connecte en tant que surveillant
            // le path est de la forme /token/token_id/token_value
            try {
                int id = Integer.parseInt(pathInfos[2]);
                String token = pathInfos[3];

                Utilisateur utilisateur = utilisateurRepository.findByIdActif(id);
                if (utilisateur != null) {
                    // vérification du token d'authentification
                    String tokenAuth = TokenFactory.generationTokenAuth(id+"", utilisateur.getDateCreation());

                    if (token.equals(tokenAuth)) {
                        // on crée une valeur en session pour ne pas passer le cas
                        req.getSession(true).setAttribute("tokenUser", utilisateur);
                        resp.sendRedirect(applicationBootstrap.getAppConfig().getUrl());
                        return;
                    }

                } else {
                    logger.error("Requête de connexion invalide, l'utilisateur n'existe pas "+pathInfos[2]);
                }
            } catch (NumberFormatException e) {
                logger.error("Requête de connexion invalide, l'id est incorrecte "+pathInfos[2]);
            }
        } else {
            logger.error("Requête de connexion invalide, le path est incorrect "+req.getRequestURI());

        }
        resp.sendRedirect(applicationBootstrap.getAppConfig().getUrl()+"/error.token.html");
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
