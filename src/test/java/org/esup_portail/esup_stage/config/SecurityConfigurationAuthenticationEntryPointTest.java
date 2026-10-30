package org.esup_portail.esup_stage.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigurationAuthenticationEntryPointTest {

    private final SecurityConfiguration securityConfiguration = new SecurityConfiguration();

    @Test
    void respondsUnauthorizedForApiWithoutContextPath() throws Exception {
        MockHttpServletResponse response = commence("", "/api/users/connected");

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    void respondsUnauthorizedForApiWithContextPath() throws Exception {
        MockHttpServletResponse response = commence("/esup-stage", "/esup-stage/api/users/connected");

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    void redirectsToCasWithinContextPath() throws Exception {
        MockHttpServletResponse response = commence("/esup-stage", "/esup-stage/frontend/");

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl()).isEqualTo("/esup-stage/login/cas");
    }

    @Test
    void redirectsToCasWithinContextPathForAnyOtherPage() throws Exception {
        MockHttpServletResponse response = commence("/esup-stage", "/esup-stage/une-page");

        assertThat(response.getRedirectedUrl()).isEqualTo("/esup-stage/login/cas");
    }

    @Test
    void redirectsToCasAtHostRootWithoutContextPath() throws Exception {
        MockHttpServletResponse response = commence("", "/une-page");

        assertThat(response.getRedirectedUrl()).isEqualTo("/login/cas");
    }

    private MockHttpServletResponse commence(String contextPath, String requestUri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath(contextPath);
        request.setRequestURI(requestUri);
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityConfiguration.commenceAuthentication(
                request, response, new InsufficientAuthenticationException("session absente"));

        return response;
    }
}
