package org.esup_portail.esup_stage.service.ldap;

import com.sun.net.httpserver.HttpServer;
import org.esup_portail.esup_stage.config.properties.ReferentielProperties;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class LdapServiceTest {

    private HttpServer server;
    private AtomicReference<String> rawQuery;
    private LdapService service;

    @BeforeEach
    void setUp() throws IOException {
        rawQuery = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/bySupannAliasLogin", exchange -> {
            rawQuery.set(exchange.getRequestURI().getRawQuery());
            byte[] body = "{\"supannAliasLogin\":\"ldap-user\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        ReferentielProperties properties = new ReferentielProperties();
        properties.setLogin("login");
        properties.setPassword("password");
        properties.setLdapUrl("http://localhost:" + server.getAddress().getPort());

        service = new LdapService(WebClient.builder());
        ReflectionTestUtils.setField(service, "referentielProperties", properties);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"john_doe", "john-doe", "john&doe", "john%doe"})
    void searchByLoginEncodesSpecialCharacters(String login) {
        LdapUser user = service.searchByLogin(login);

        assertThat(user.getSupannAliasLogin()).isEqualTo("ldap-user");
        assertThat(URLDecoder.decode(rawQuery.get(), StandardCharsets.UTF_8)).isEqualTo("login=" + login);
    }
}