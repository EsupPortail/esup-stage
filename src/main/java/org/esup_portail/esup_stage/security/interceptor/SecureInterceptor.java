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

@Aspect
@Configuration
public class SecureInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SecureInterceptor.class);

    @Around("@annotation(Secure)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getStaticPart().getSignature();
        Secure authorized = methodSignature.getMethod().getAnnotation(Secure.class);
        AppFonctionEnum[] fonctions = authorized.fonctions();
        DroitEnum[] droits = authorized.droits();
        boolean forbiddenEtu = authorized.forbiddenEtu();

        Utilisateur utilisateur = ServiceContext.getUtilisateur();

        if (utilisateur == null) {
            return joinPoint.proceed();
        }

        boolean hasRight = fonctions.length == 0 && droits.length == 0;

        if (!hasRight && fonctions.length > 0 && droits.length > 0) {
            hasRight = UtilisateurHelper.isRole(utilisateur, fonctions, droits);
        }

        if (forbiddenEtu && UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            hasRight = false;
        }

        if (!hasRight) {
            throw new AppException(HttpStatus.FORBIDDEN, "Votre rôle ne donne pas accès à cette ressource");
        }

        return joinPoint.proceed();
    }
}
