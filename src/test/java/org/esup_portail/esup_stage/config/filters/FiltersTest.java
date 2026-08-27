package org.esup_portail.esup_stage.config.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.config.properties.signature.WebhookProperties;
import org.esup_portail.esup_stage.dto.MaintenanceStateDto;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.service.maintenance.MaintenanceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests des filtres HTTP : jeton public, jeton webhook, cookie CSRF
 * et mode maintenance.
 */
class FiltersTest {

    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        chain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest requete(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        return request;
    }

    // ------------------------------------------------------------------
    // PublicTokenFilter
    // ------------------------------------------------------------------

    private PublicTokenFilter publicTokenFilter() {
        AppliProperties appliProperties = new AppliProperties();
        appliProperties.setTokens(List.of("token-1", "token-2"));
        return new PublicTokenFilter(appliProperties);
    }

    @Test
    void leJetonPublicValideAuthentifie() throws Exception {
        MockHttpServletRequest request = requete("/public/api/conventions");
        request.addHeader("Authorization", "Bearer token-1");

        publicTokenFilter().doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void leJetonPublicInvalideEstRejete() {
        MockHttpServletRequest request = requete("/public/api/conventions");
        request.addHeader("Authorization", "Bearer pirate");

        assertThatThrownBy(() -> publicTokenFilter().doFilter(request, response, chain))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void sansJetonLaRequetePubliquePasseSansAuthentification() throws Exception {
        publicTokenFilter().doFilter(requete("/public/api/conventions"), response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void lesautresCheminsNeSontPasFiltres() {
        PublicTokenFilter filter = publicTokenFilter();

        assertThat(filter.shouldNotFilter(requete("/api/conventions"))).isTrue();
        assertThat(filter.shouldNotFilter(requete("/public/api"))).isFalse();
    }

    // ------------------------------------------------------------------
    // WebhookEsupSignatureTokenFilter
    // ------------------------------------------------------------------

    private WebhookEsupSignatureTokenFilter webhookFilter() {
        WebhookEsupSignatureTokenFilter filter = new WebhookEsupSignatureTokenFilter();
        WebhookProperties webhookProperties = new WebhookProperties();
        webhookProperties.setToken("webhook-token");
        SignatureProperties signatureProperties = new SignatureProperties(
                new org.esup_portail.esup_stage.config.properties.signature.DocaposteProperties(),
                webhookProperties,
                new org.esup_portail.esup_stage.config.properties.signature.EsupSignatureProperties());
        ReflectionTestUtils.setField(filter, "signatureProperties", signatureProperties);
        return filter;
    }

    @Test
    void leJetonWebhookEstControle() throws Exception {
        MockHttpServletRequest request = requete("/webhook/esup");
        request.addHeader("Authorization", "Bearer webhook-token");
        webhookFilter().doFilter(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

        MockHttpServletRequest mauvaise = requete("/webhook/esup");
        mauvaise.addHeader("Authorization", "Bearer intrus");
        assertThatThrownBy(() -> webhookFilter().doFilter(mauvaise, response, new MockFilterChain()))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(webhookFilter().shouldNotFilter(requete("/api/x"))).isTrue();
    }

    // ------------------------------------------------------------------
    // CsrfCookieFilter
    // ------------------------------------------------------------------

    @Test
    void leCookieCsrfEstForceQuandLeJetonExiste() throws Exception {
        MockHttpServletRequest request = requete("/api/conventions");
        CsrfToken token = mock(CsrfToken.class);
        when(token.getToken()).thenReturn("csrf");
        request.setAttribute(CsrfToken.class.getName(), token);

        new CsrfCookieFilter().doFilter(request, response, chain);

        verify(token).getToken();
        assertThat(chain.getRequest()).isNotNull();

        // sans jeton : la chaîne continue simplement
        new CsrfCookieFilter().doFilter(requete("/api/x"), new MockHttpServletResponse(), new MockFilterChain());
    }

    // ------------------------------------------------------------------
    // MaintenanceModeFilter
    // ------------------------------------------------------------------

    private MaintenanceModeFilter maintenanceFilter(boolean active) {
        MaintenanceModeFilter filter = new MaintenanceModeFilter();
        MaintenanceService maintenanceService = mock(MaintenanceService.class);
        MaintenanceStateDto state = new MaintenanceStateDto();
        state.setActive(active);
        when(maintenanceService.getLastKnownState()).thenReturn(state);
        AppliProperties appliProperties = new AppliProperties();
        appliProperties.setAdminTechnique("admintech1");
        ReflectionTestUtils.setField(filter, "maintenanceService", maintenanceService);
        ReflectionTestUtils.setField(filter, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(filter, "appliProperties", appliProperties);
        return filter;
    }

    @Test
    void horsMaintenanceLesRequetesPassent() throws Exception {
        maintenanceFilter(false).doFilter(requete("/api/conventions"), response, chain);
        assertThat(response.getStatus()).isNotEqualTo(503);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void enMaintenanceLesApiSontBloqueesEn503() throws Exception {
        MockHttpServletResponse blocked = new MockHttpServletResponse();

        maintenanceFilter(true).doFilter(requete("/api/conventions"), blocked, new MockFilterChain());

        assertThat(blocked.getStatus()).isEqualTo(503);
        assertThat(blocked.getContentAsString()).contains("Application en maintenance");
    }

    @Test
    void enMaintenanceLesUrisExcluesEtLesNonApiPassent() throws Exception {
        MaintenanceModeFilter filter = maintenanceFilter(true);

        MockFilterChain chainStatus = new MockFilterChain();
        filter.doFilter(requete("/api/maintenance/status"), new MockHttpServletResponse(), chainStatus);
        assertThat(chainStatus.getRequest()).isNotNull();

        MockFilterChain chainFront = new MockFilterChain();
        filter.doFilter(requete("/frontend/index.html"), new MockHttpServletResponse(), chainFront);
        assertThat(chainFront.getRequest()).isNotNull();
    }

    @Test
    void enMaintenanceLAdminPasse() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority(Role.ADM))));

        MockFilterChain chainAdmin = new MockFilterChain();
        maintenanceFilter(true).doFilter(requete("/api/conventions"), new MockHttpServletResponse(), chainAdmin);

        assertThat(chainAdmin.getRequest()).isNotNull();
    }

    @Test
    void enMaintenanceLAdminTechniquePasseParSonLogin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admintech1", null, List.of()));

        MockFilterChain chainTech = new MockFilterChain();
        maintenanceFilter(true).doFilter(requete("/api/conventions"), new MockHttpServletResponse(), chainTech);

        assertThat(chainTech.getRequest()).isNotNull();
    }
}
