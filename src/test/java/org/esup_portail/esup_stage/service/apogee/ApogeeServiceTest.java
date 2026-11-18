package org.esup_portail.esup_stage.service.apogee;

import org.esup_portail.esup_stage.config.properties.ReferentielProperties;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de ApogeeService.getInfoApogee(...) sur un annuaire multi-SI :
 * - 404 côté Apogée (étudiant présent dans le LDAP commun mais absent du SI courant) => AppException NOT_FOUND
 * - réponse nominale => EtudiantRef renseigné
 * - erreur technique => AppException propagée
 */
class ApogeeServiceTest {

    private ApogeeService service;
    private ReferentielProperties referentielProperties;
    private final AtomicReference<String> lastUri = new AtomicReference<>();

    @BeforeEach
    void setup() {
        referentielProperties = mock(ReferentielProperties.class);
        when(referentielProperties.getApogeeUrl()).thenReturn("http://apogee.test");
        when(referentielProperties.getLogin()).thenReturn("login");
        when(referentielProperties.getPassword()).thenReturn("password");

        service = new ApogeeService(WebClient.builder().exchangeFunction(respondWith(HttpStatus.OK, "{}")));
        ReflectionTestUtils.setField(service, "referentielProperties", referentielProperties);
    }

    private ExchangeFunction respondWith(HttpStatus status, String body) {
        return request -> {
            lastUri.set(request.url().toString());
            return Mono.just(ClientResponse.create(status)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                    .body(body)
                    .build());
        };
    }

    private void givenApogeeResponds(HttpStatus status, String body) {
        service = new ApogeeService(WebClient.builder().exchangeFunction(respondWith(status, body)));
        ReflectionTestUtils.setField(service, "referentielProperties", referentielProperties);
    }

    @Test
    void getInfoApogee_propageUn404QuandLEtudiantEstAbsentDuSiCourant() {
        givenApogeeResponds(HttpStatus.NOT_FOUND, "");

        assertThatThrownBy(() -> service.getInfoApogee("12345678", "2026"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getHttpStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(lastUri.get()).contains("/etudiantRef").contains("codEtud=12345678");
    }

    @Test
    void getInfoApogee_retourneLEtudiantQuandApogeeRepond() {
        givenApogeeResponds(HttpStatus.OK, "{\"cod_ind\":42,\"nompatro\":\"DURAND\",\"prenom\":\"Alice\",\"mail\":\"alice.durand@univ.fr\"}");

        EtudiantRef result = service.getInfoApogee("12345678", "2026");

        assertThat(result).isNotNull();
        assertThat(result.getNompatro()).isEqualTo("DURAND");
        assertThat(result.getPrenom()).isEqualTo("Alice");
    }

    @Test
    void getInfoApogee_propageUneErreurTechnique() {
        // 500 sur /etudiantRef => "Une erreur technique est survenue." (l'erreur WebClient est traduite par call())
        givenApogeeResponds(HttpStatus.INTERNAL_SERVER_ERROR, "");

        assertThatThrownBy(() -> service.getInfoApogee("12345678", "2026"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getHttpStatus())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getInfoApogee_completeLeMailDepuisLeLdapQuandApogeeLeRenvoieVide() {
        givenApogeeResponds(HttpStatus.OK, "{\"cod_ind\":42,\"nompatro\":\"DURAND\",\"prenom\":\"Alice\"}");
        LdapService ldapService = mock(LdapService.class);
        LdapUser ldapUser = new LdapUser();
        ldapUser.setMail("alice.durand@univ.fr");
        when(ldapService.search(eq("/etudiant"), any(LdapSearchDto.class))).thenReturn(List.of(ldapUser));
        ReflectionTestUtils.setField(service, "ldapService", ldapService);

        EtudiantRef result = service.getInfoApogee("12345678", "2026");

        assertThat(result).isNotNull();
        assertThat(result.getMail()).isEqualTo("alice.durand@univ.fr");
    }
}
