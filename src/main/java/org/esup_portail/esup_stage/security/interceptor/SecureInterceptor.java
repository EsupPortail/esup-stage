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
import org.esup_portail.esup_stage.security.permission.PermissionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Aspect
@Configuration
public class SecureInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SecureInterceptor.class);

    @Autowired
    private ApplicationContext context;

    @Around("@annotation(Secure)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
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
            throw new AppException(HttpStatus.FORBIDDEN, "Votre role ne donne pas acces a cette ressource");
        }

        Class<? extends PermissionEvaluator> evaluatorClass = authorized.evaluator();
        if (!PermissionEvaluator.class.equals(evaluatorClass)) {
            PermissionEvaluator evaluator = context.getBean(evaluatorClass);
            boolean hasPermission = evaluator.hasPermission(utilisateur, methodSignature, joinPoint.getArgs());
            if (!hasPermission) {
                throw new AppException(HttpStatus.FORBIDDEN, "Vous n'avez pas acces a cette ressource");
            }
        }

        return joinPoint.proceed();
    }
}
