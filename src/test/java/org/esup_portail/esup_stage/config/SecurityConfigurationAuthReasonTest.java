package org.esup_portail.esup_stage.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Le motif renvoyé sur un 401 pilote l'affichage de la fenêtre de reconnexion côté frontend :
 * seule une session réellement perdue doit produire "idle".
 */
class SecurityConfigurationAuthReasonTest {

    @Test
    void aucunCookieDeSessionSignifiePremierAcces() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestedSessionId()).thenReturn(null);

        assertThat(resolveAuthReason(request)).isEqualTo("no-session");
    }

    @Test
    void unCookieDeSessionPerimeSignifieSessionExpiree() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestedSessionId()).thenReturn("ABCDEF0123456789");
        when(request.isRequestedSessionIdValid()).thenReturn(false);

        assertThat(resolveAuthReason(request)).isEqualTo("idle");
    }

    @Test
    void uneSessionValideMaisNonAuthentifieeNestPasUneExpiration() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestedSessionId()).thenReturn("ABCDEF0123456789");
        when(request.isRequestedSessionIdValid()).thenReturn(true);

        assertThat(resolveAuthReason(request)).isEqualTo("no-session");
    }

    private String resolveAuthReason(HttpServletRequest request) {
        return ReflectionTestUtils.invokeMethod(new SecurityConfiguration(), "resolveAuthReason", request);
    }
}
