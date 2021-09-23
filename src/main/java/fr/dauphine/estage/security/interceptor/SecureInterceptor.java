package fr.dauphine.estage.security.interceptor;

import fr.dauphine.estage.dto.ContextDto;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.ForbiddenException;
import fr.dauphine.estage.exception.NotAcceptableException;
import fr.dauphine.estage.exception.UnauthorizedException;
import fr.dauphine.estage.model.Utilisateur;
import fr.dauphine.estage.model.helper.UtilisateurHelper;
import fr.dauphine.estage.repository.UtilisateurJpaRepository;
import fr.dauphine.estage.security.ServiceContext;
import fr.dauphine.estage.security.TokenFactory;
import fr.dauphine.estage.security.common.CasLayer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Aspect
@Configuration
public class SecureInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SecureInterceptor.class);

    @Autowired
    UtilisateurJpaRepository utilisateurRepository;

    @Around("@annotation(Secure)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = currentRequest();

        if (request == null) {
            throw new Exception("Not a request");
        }

        MethodSignature methodSignature = (MethodSignature) joinPoint.getStaticPart().getSignature();
        Secure authorized = methodSignature.getMethod().getAnnotation(Secure.class);
        AppFonctionEnum fonction = authorized.fonction();
        DroitEnum[] droits = authorized.droits();

        // validation du token
        String token = null;
        if (request.getParameter("token") != null) {
            token = new String(Base64.getDecoder().decode(request.getParameter("token")));
        }

        String login;
        String auth;
        if (token != null && TokenFactory.matchTokenPattern(token)) {
            String[] tokenAttributes = token.split("\\|");
            login = tokenAttributes[0];
            auth = tokenAttributes[1];
            String date = tokenAttributes[2];
            String hash = tokenAttributes[3];

            try {
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                Date dateTicket = df.parse(date);
                Date nDate = new Date();
                long diffMinutes = TimeUnit.MINUTES.convert(nDate.getTime() - dateTicket.getTime(), TimeUnit.MILLISECONDS);
                if (diffMinutes > Integer.parseInt("1800") || dateTicket.after(nDate)) {
                    logger.info(" -> token expire");
                    throw new UnauthorizedException("Token invalide");
                }
                if (!hash.equals(TokenFactory.generateHash(login, date, "Azkjn32klklOP"))) {
                    throw new UnauthorizedException("Token invalide");
                }
            } catch (ParseException e) {
                logger.info(" -> format date invalid");
                throw new UnauthorizedException("Token invalide");
            }


        } else {
            // on check si on a pas une authentification cas
            CasLayer.CasUser casUser = (CasLayer.CasUser) request.getSession().getAttribute("casUser");
            if (casUser == null) {
                throw new UnauthorizedException("Token invalide");
            } else {
                login = casUser.getLogin();
                auth = "cas";
            }
        }

        // get the user
        Utilisateur utilisateur;
        if (auth.equals("cas")) {
            utilisateur = utilisateurRepository.findOneByLoginAcitf(login);
        } else {
            utilisateur = utilisateurRepository.findByIdActif(Integer.parseInt(login));
        }

        if (utilisateur == null) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé");
        }

        // Utilisateur not active
        if (!utilisateur.isActif()) {
            throw new NotAcceptableException("Votre compte est inactif");
        }

        boolean hasRight = fonction == AppFonctionEnum.NONE && droits.length == 0;
        // on a les droits si fonction droit == none
        if (!hasRight) {
            // Si une fonction/droit est définie, il faut vérifier ses habilitations
            if (fonction != AppFonctionEnum.NONE && droits.length > 0) {
                if (UtilisateurHelper.isRole(utilisateur, fonction, droits)) {
                    hasRight = true;
                }
            }
        }
        if (!hasRight) {
            throw new ForbiddenException("Votre rôle de donne pas accès à cette ressource");
        }

        ////////////////////////////////////
        // on crée le contexte de service
        ////////////////////////////////////

        ContextDto context = new ContextDto(utilisateur, auth);
        ServiceContext.initialize(context);
        try {
            return joinPoint.proceed();
        } finally {
            ServiceContext.cleanup();
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Optional.ofNullable(servletRequestAttributes).map(ServletRequestAttributes::getRequest).orElse(null);
    }
}
