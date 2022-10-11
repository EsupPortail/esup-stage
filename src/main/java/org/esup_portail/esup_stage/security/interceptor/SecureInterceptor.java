package org.esup_portail.esup_stage.security.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Aspect
@Configuration
public class SecureInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SecureInterceptor.class);

    @Around("@annotation(Secure)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = currentRequest();

        if (request == null) {
            throw new Exception("Not a request");
        }

        MethodSignature methodSignature = (MethodSignature) joinPoint.getStaticPart().getSignature();
        Secure authorized = methodSignature.getMethod().getAnnotation(Secure.class);
        AppFonctionEnum[] fonctions = authorized.fonctions();
        DroitEnum[] droits = authorized.droits();
        boolean forbiddenEtu = authorized.forbiddenEtu();

        Utilisateur utilisateur = ServiceContext.getUtilisateur();

        if (utilisateur == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Vous n'êtes pas autorisé");
        }

        // Utilisateur not active
        if (!utilisateur.isActif()) {
            throw new AppException(HttpStatus.NOT_ACCEPTABLE, "Votre compte est inactif");
        }

        boolean hasRight = fonctions.length == 0 && droits.length == 0;
        // on a les droits si fonction droit == none
        if (!hasRight) {
            // Si une fonction/droit est définie, il faut vérifier ses habilitations
            if (fonctions.length > 0 && droits.length > 0) {
                if (UtilisateurHelper.isRole(utilisateur, fonctions, droits)) {
                    hasRight = true;
                }
            }
        }

        // On n'a pas les droits si le rôle fait partie de ceux interdit
        if (forbiddenEtu && UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            hasRight = false;
        }

        if (!hasRight) {
            throw new AppException(HttpStatus.FORBIDDEN, "Votre rôle de donne pas accès à cette ressource");
        }

        return joinPoint.proceed();
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Optional.ofNullable(servletRequestAttributes).map(ServletRequestAttributes::getRequest).orElse(null);
    }
}
