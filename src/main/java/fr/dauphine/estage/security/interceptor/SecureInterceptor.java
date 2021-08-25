package fr.dauphine.estage.security.interceptor;

import fr.dauphine.estage.exception.UnauthorizedException;
import fr.dauphine.estage.model.Utilisateur;
import fr.dauphine.estage.security.TokenFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Around("@annotation(Secure)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = currentRequest();

        if (request == null) {
            throw new Exception("Not a request");
        }

        MethodSignature methodSignature = (MethodSignature) joinPoint.getStaticPart().getSignature();
        Secure authorized = methodSignature.getMethod().getAnnotation(Secure.class);
        String[] roles = authorized.roles();

        // validation du token
        String token = null;
        if (request.getParameter("token")!=null) {
            token = new String(Base64.getDecoder().decode(request.getParameter("token")));
        }

        String login = null;
        if( token!=null && TokenFactory.matchTokenPattern(token)){
            String[] tokenAttributes = token.split("\\|");
            login = tokenAttributes[0];
            String date = tokenAttributes[1];
            String hash = tokenAttributes[2];

            try {
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                Date dateTicket = df.parse(date);
                Date nDate	=new Date();
                long diffMinutes	= TimeUnit.MINUTES.convert(nDate.getTime() - dateTicket.getTime(),TimeUnit.MILLISECONDS);
                if(diffMinutes > Integer.parseInt("1800") || dateTicket.after(nDate)) {
                    logger.info(" -> token expire");
                    throw new UnauthorizedException("TOKEN_ERROR_INVALID");
                }
                if(!hash.equals(TokenFactory.generateHash(login, date, "Azkjn32klklOP"))) {
                    throw new UnauthorizedException("TOKEN_ERROR_INVALID");
                }
            } catch (ParseException e) {
                logger.info(" -> format date invalid");
                throw new UnauthorizedException("TOKEN_ERROR_INVALID");
            }


        } else {
            throw new UnauthorizedException("Vous devez disposer d'un jeton valide pour vous connecter à l'application");
        }

        // get the user
//        Utilisateur utilisateur = utilisateurDao.getUtilisateurByUsername(login);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setDisplayname("test");
        utilisateur.setUsername("usernametest");
        if (utilisateur==null) {
            throw new UnauthorizedException("Vous devez disposer d'un jeton valide pour vous connecter à l'application");
        }

        // si des roles sont précisés, il faut vérifier que l'utilisateur en dispose
        if (roles!=null && roles.length>0) {
            boolean hasRight=false;
            for(String role:roles){
                if (role.equals(utilisateur.getRole())) {
                    hasRight=true;
                }
            }
            if (!hasRight) {
                throw new UnauthorizedException("L'accès à cette ressource est interdit avec votre rôle");
            }
        }

        // on stocke l'utilisateur dans la request pour le transmettre aux services
        request.setAttribute("utilisateur",utilisateur);

        return joinPoint.proceed();
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Optional.ofNullable(servletRequestAttributes).map(ServletRequestAttributes::getRequest).orElse(null);
    }
}
