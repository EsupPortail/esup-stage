package secure;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.AppFonction;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.RoleAppFonction;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.security.interceptor.SecureInterceptor;
import org.esup_portail.esup_stage.security.permission.PermissionEvaluator;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecureInterceptorTests.TestConfig.class)
class SecureInterceptorTests {

    @Autowired
    private SecureInterceptor secureInterceptor;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_forbid_when_evaluator_denies_access() throws Throwable {
        setAuthenticatedUserWithModificationRight();
        ProceedingJoinPoint joinPoint = buildJoinPoint("deny", 42);

        AppException exception = assertThrows(AppException.class, () -> secureInterceptor.checkAuthorization(joinPoint));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void should_proceed_when_evaluator_grants_access() throws Throwable {
        setAuthenticatedUserWithModificationRight();
        ProceedingJoinPoint joinPoint = buildJoinPoint("allow", 42);

        Object result = secureInterceptor.checkAuthorization(joinPoint);

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
    }

    private ProceedingJoinPoint buildJoinPoint(String methodName, Object... args) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature methodSignature = mock(MethodSignature.class);
        Method method = TestSecuredMethods.class.getMethod(methodName, Integer.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn("ok");
        when(methodSignature.getMethod()).thenReturn(method);

        return joinPoint;
    }

    private void setAuthenticatedUserWithModificationRight() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin("user");
        utilisateur.setActif(true);
        utilisateur.setRoles(List.of(buildRoleWithModificationRight()));

        CasUserDetailsImpl principal = new CasUserDetailsImpl(utilisateur, Collections.emptyList());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Role buildRoleWithModificationRight() {
        AppFonction appFonction = new AppFonction();
        appFonction.setCode(AppFonctionEnum.SERVICE_CONTACT_ACC);

        RoleAppFonction roleAppFonction = new RoleAppFonction();
        roleAppFonction.setAppFonction(appFonction);
        roleAppFonction.setModification(true);

        Role role = new Role();
        role.setCode(Role.GES);
        role.setRoleAppFonctions(List.of(roleAppFonction));
        return role;
    }

    static class TestSecuredMethods {
        @Secure(
                fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC},
                droits = {DroitEnum.MODIFICATION},
                evaluator = DenyEvaluator.class
        )
        public void deny(Integer id) {
        }

        @Secure(
                fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC},
                droits = {DroitEnum.MODIFICATION},
                evaluator = AllowEvaluator.class
        )
        public void allow(Integer id) {
        }
    }

    static class DenyEvaluator implements PermissionEvaluator {
        @Override
        public boolean hasPermission(Utilisateur user, MethodSignature sig, Object[] args) {
            return false;
        }
    }

    static class AllowEvaluator implements PermissionEvaluator {
        @Override
        public boolean hasPermission(Utilisateur user, MethodSignature sig, Object[] args) {
            return true;
        }
    }

    @Configuration
    static class TestConfig {
        @Bean
        SecureInterceptor secureInterceptor() {
            return new SecureInterceptor();
        }

        @Bean
        DenyEvaluator denyEvaluator() {
            return new DenyEvaluator();
        }

        @Bean
        AllowEvaluator allowEvaluator() {
            return new AllowEvaluator();
        }
    }
}