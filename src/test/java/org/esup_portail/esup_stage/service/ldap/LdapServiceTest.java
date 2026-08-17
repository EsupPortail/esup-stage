package org.esup_portail.esup_stage.service.ldap;

import org.esup_portail.esup_stage.config.properties.ReferentielProperties;
import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Le web service LDAP est simulé par une ExchangeFunction : {@code corpsReponse}
 * fixe la réponse JSON et {@code statutForce} le code HTTP.
 */
class LdapServiceTest {

    private LdapService service;
    private ReferentielProperties referentielProperties;
    private String corpsReponse = "[]";
    private HttpStatus statutForce = HttpStatus.OK;
    private final AtomicReference<String> derniereUrl = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        ExchangeFunction exchange = request -> {
            derniereUrl.set(request.url().toString());
            return Mono.just(ClientResponse.create(statutForce)
                    .header("Content-Type", "application/json")
                    .body(corpsReponse)
                    .build());
        };
        service = new LdapService(WebClient.builder().exchangeFunction(exchange));
        referentielProperties = mock(ReferentielProperties.class);
        when(referentielProperties.getLdapUrl()).thenReturn("http://ldap.test");
        when(referentielProperties.getLogin()).thenReturn("user");
        when(referentielProperties.getPassword()).thenReturn("secret");
        ReflectionTestUtils.setField(service, "referentielProperties", referentielProperties);
    }

    @Test
    void searchRetourneLesUtilisateursTrouves() {
        corpsReponse = "[{\"uid\":\"etu1\",\"mail\":\"etu1@univ.fr\"},{\"uid\":\"etu2\"}]";
        LdapSearchDto criteres = new LdapSearchDto();
        criteres.setCodEtu("12345");

        List<LdapUser> utilisateurs = service.search("/etudiant", criteres);

        assertThat(utilisateurs).hasSize(2);
        assertThat(utilisateurs.get(0).getUid()).isEqualTo("etu1");
        assertThat(utilisateurs.get(0).getMail()).isEqualTo("etu1@univ.fr");
        assertThat(derniereUrl.get()).isEqualTo("http://ldap.test/etudiant");
    }

    @Test
    void searchEchoueSurUneReponseIllisible() {
        corpsReponse = "pas du json";

        assertThatThrownBy(() -> service.search("/etudiant", new LdapSearchDto()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("technique");
    }

    @Test
    void searchEchoueSurUneErreurHttp() {
        statutForce = HttpStatus.INTERNAL_SERVER_ERROR;

        assertThatThrownBy(() -> service.search("/etudiant", new LdapSearchDto()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("technique");
    }

    @Test
    void searchExigeDesIdentifiantsConfigures() {
        when(referentielProperties.getLogin()).thenReturn(null);

        assertThatThrownBy(() -> service.search("/etudiant", new LdapSearchDto()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("technique");
    }

    @Test
    void searchByLoginRetourneLUtilisateur() {
        corpsReponse = "{\"uid\":\"prof1\",\"mail\":\"prof1@univ.fr\"}";

        LdapUser utilisateur = service.searchByLogin("prof1");

        assertThat(utilisateur.getUid()).isEqualTo("prof1");
        assertThat(derniereUrl.get()).startsWith("http://ldap.test/bySupannAliasLogin").contains("login=prof1");
    }

    @Test
    void searchByLoginSansLoginRetourneNull() {
        assertThat(service.searchByLogin(null)).isNull();
        assertThat(service.searchByLogin("")).isNull();
    }

    @Test
    void searchByLoginSansReponseRetourneNull() {
        corpsReponse = "";

        assertThat(service.searchByLogin("inconnu")).isNull();
    }

    @Test
    void searchByLoginEchoueSurUneReponseIllisible() {
        corpsReponse = "pas du json";

        assertThatThrownBy(() -> service.searchByLogin("prof1"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("technique");
    }
}
