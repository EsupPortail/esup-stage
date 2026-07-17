package org.esup_portail.esup_stage.service.proprety;

import org.esup_portail.esup_stage.dto.DocaposteTestRequestDto;
import org.esup_portail.esup_stage.dto.EsupSignatureTestRequestDto;
import org.esup_portail.esup_stage.dto.MailerTestRequestDto;
import org.esup_portail.esup_stage.dto.SireneTestRequestDto;
import org.esup_portail.esup_stage.dto.WebhookTestRequestDto;
import org.esup_portail.esup_stage.model.AppProperty;
import org.esup_portail.esup_stage.repository.AppPropertyJpaRepository;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.Composante;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppProperyServiceTest {

    private AppProperyService service;
    private AppPropertyJpaRepository appPropertyJpaRepository;
    private ApogeeService apogeeService;
    private ApplicationEventPublisher applicationEventPublisher;
    private PropertyCryptoService propertyCryptoService;

    @BeforeEach
    void setUp() {
        service = new AppProperyService();
        appPropertyJpaRepository = mock(AppPropertyJpaRepository.class);
        apogeeService = mock(ApogeeService.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        propertyCryptoService = mock(PropertyCryptoService.class);
        ReflectionTestUtils.setField(service, "appPropertyJpaRepository", appPropertyJpaRepository);
        ReflectionTestUtils.setField(service, "apogeeService", apogeeService);
        ReflectionTestUtils.setField(service, "applicationEventPublisher", applicationEventPublisher);
        ReflectionTestUtils.setField(service, "propertyCryptoService", propertyCryptoService);
    }

    private AppProperty property(String key, String value, boolean secret, String encrypted) {
        AppProperty appProperty = new AppProperty();
        appProperty.setKey(key);
        appProperty.setValue(value);
        appProperty.setIsSecret(secret);
        appProperty.setValueEncrypted(encrypted);
        return appProperty;
    }

    // ------------------------------------------------------------------
    // getOverrides
    // ------------------------------------------------------------------

    @Test
    void lesOverridesIgnorentLesEntreesInexploitables() {
        when(appPropertyJpaRepository.findAll()).thenReturn(Arrays.asList(
                null,
                property("", "ignoree", false, null),
                property("appli.url", "", false, null),
                property("appli.nom", "esup-stage", false, null)
        ));

        Map<String, String> overrides = service.getOverrides();

        assertThat(overrides).containsExactly(Map.entry("appli.nom", "esup-stage"));
    }

    @Test
    void lesSecretsSontDechiffres() {
        when(appPropertyJpaRepository.findAll()).thenReturn(List.of(
                property("sirene.token", null, true, "CHIFFRE"),
                property("appli.mailer.password", null, true, null) // secret sans valeur : ignoré
        ));
        when(propertyCryptoService.decrypt("CHIFFRE")).thenReturn("token-en-clair");

        Map<String, String> overrides = service.getOverrides();

        assertThat(overrides).containsExactly(Map.entry("sirene.token", "token-en-clair"));
    }

    @Test
    void unSecretIndechiffrableEstIgnoreSansCasserLesAutres() {
        when(appPropertyJpaRepository.findAll()).thenReturn(List.of(
                property("sirene.token", null, true, "CHIFFRE"),
                property("appli.nom", "esup-stage", false, null)
        ));
        when(propertyCryptoService.decrypt("CHIFFRE")).thenReturn(null);

        Map<String, String> overrides = service.getOverrides();

        assertThat(overrides).containsExactly(Map.entry("appli.nom", "esup-stage"));
    }

    @Test
    void lePredicatDeFiltrageExclutDesCles() {
        when(appPropertyJpaRepository.findAll()).thenReturn(List.of(
                property("appli.nom", "esup-stage", false, null),
                property("appli.url", "https://stage.fr", false, null)
        ));

        Map<String, String> overrides = service.getOverrides(key -> key.equals("appli.url"));

        assertThat(overrides).containsOnlyKeys("appli.nom");
    }

    // ------------------------------------------------------------------
    // save
    // ------------------------------------------------------------------

    @Test
    void saveIgnoreLesClesVidesOuInconnues() {
        service.save(" ", "valeur");
        verify(appPropertyJpaRepository, never()).findByKey(any());

        when(appPropertyJpaRepository.findByKey("inconnue")).thenReturn(null);
        service.save("inconnue", "valeur");
        verify(appPropertyJpaRepository, never()).save(any());
    }

    @Test
    void saveChiffreLesClesSecretes() {
        AppProperty appProperty = property("sirene.token", "ancienne", false, null);
        when(appPropertyJpaRepository.findByKey("sirene.token")).thenReturn(appProperty);
        when(propertyCryptoService.encrypt("nouveau-token")).thenReturn("CHIFFRE");

        service.save("sirene.token", "nouveau-token");

        assertThat(appProperty.getIsSecret()).isTrue();
        assertThat(appProperty.getValueEncrypted()).isEqualTo("CHIFFRE");
        assertThat(appProperty.getValue()).isNull();
        assertThat(appProperty.getUpdatedAt()).isNotNull();
        verify(appPropertyJpaRepository).save(appProperty);
        verify(applicationEventPublisher).publishEvent(any(org.esup_portail.esup_stage.config.properties.ConfigReloadEvent.class));
    }

    @Test
    void saveSecretAvecValeurNulleNeToucheQueLaDate() {
        AppProperty appProperty = property("appli.jwt_secret", null, true, "ANCIEN");
        when(appPropertyJpaRepository.findByKey("appli.jwt_secret")).thenReturn(appProperty);

        service.save("appli.jwt_secret", null);

        assertThat(appProperty.getValueEncrypted()).isEqualTo("ANCIEN");
        verify(appPropertyJpaRepository).save(appProperty);
        verify(applicationEventPublisher).publishEvent(any(org.esup_portail.esup_stage.config.properties.ConfigReloadEvent.class));
    }

    @Test
    void saveSecretAvecValeurBlancheEfface() {
        AppProperty appProperty = property("appli.jwt_secret", null, true, "ANCIEN");
        when(appPropertyJpaRepository.findByKey("appli.jwt_secret")).thenReturn(appProperty);

        service.save("appli.jwt_secret", "  ");

        assertThat(appProperty.getValueEncrypted()).isNull();
    }

    @Test
    void saveStockeLesClesNonSecretesEnClair() {
        AppProperty appProperty = property("appli.nom", "ancien", false, "RESIDU");
        when(appPropertyJpaRepository.findByKey("appli.nom")).thenReturn(appProperty);

        service.save("appli.nom", "nouveau");

        assertThat(appProperty.getValue()).isEqualTo("nouveau");
        assertThat(appProperty.getValueEncrypted()).isNull();
        assertThat(appProperty.getIsSecret()).isFalse();
        verify(propertyCryptoService, never()).encrypt(any());
    }

    // ------------------------------------------------------------------
    // tests de connexion : validation des paramètres et garde-fous SSRF
    // (aucun accès réseau : URLs rejetées avant tout appel)
    // ------------------------------------------------------------------

    @Test
    void testMailerRejetteLesParametresInvalides() {
        assertThat(service.testMailer(null).getResult()).isEqualTo("error");
        MailerTestRequestDto sansHost = new MailerTestRequestDto();
        sansHost.setPort(25);
        assertThat(service.testMailer(sansHost).getResult()).isEqualTo("error");
    }

    @Test
    void testReferentielReussitSiApogeeRepond() {
        when(apogeeService.getListComposante()).thenReturn(List.of(new Composante()));
        assertThat(service.testReferentiel(null).getResult()).isEqualTo("success");
    }

    @Test
    void testReferentielEchoueSiApogeeVideOuEnErreur() {
        when(apogeeService.getListComposante()).thenReturn(List.of());
        assertThat(service.testReferentiel(null).getResult()).isEqualTo("error");

        when(apogeeService.getListComposante()).thenThrow(new RuntimeException("panne"));
        assertThat(service.testReferentiel(null).getResult()).isEqualTo("error");
    }

    @Test
    void testWebhookRejetteLesUrisManquantesOuInterdites() {
        assertThat(service.testWebhook(null).getResult()).isEqualTo("error");

        WebhookTestRequestDto localhost = new WebhookTestRequestDto();
        localhost.setUri("http://localhost/api");
        assertThat(service.testWebhook(localhost).getMessage()).contains("interne");

        WebhookTestRequestDto ipPrivee = new WebhookTestRequestDto();
        ipPrivee.setUri("http://192.168.1.10/api");
        assertThat(service.testWebhook(ipPrivee).getMessage()).contains("interne ou privée");

        WebhookTestRequestDto ftp = new WebhookTestRequestDto();
        ftp.setUri("ftp://exemple.fr/api");
        assertThat(service.testWebhook(ftp).getMessage()).contains("HTTP/HTTPS");

        WebhookTestRequestDto credentials = new WebhookTestRequestDto();
        credentials.setUri("http://user:pass@exemple.fr/api");
        assertThat(service.testWebhook(credentials).getMessage()).contains("identifiants interdits");
    }

    @Test
    void testSireneRejetteLesParametresInvalides() {
        assertThat(service.testSirene(null).getResult()).isEqualTo("error");

        SireneTestRequestDto sansToken = new SireneTestRequestDto();
        sansToken.setUrl("https://api.insee.fr");
        assertThat(service.testSirene(sansToken).getResult()).isEqualTo("error");

        SireneTestRequestDto sansSiret = new SireneTestRequestDto();
        sansSiret.setUrl("https://api.insee.fr");
        sansSiret.setToken("token");
        assertThat(service.testSirene(sansSiret).getMessage()).contains("SIRET");

        SireneTestRequestDto urlPrivee = new SireneTestRequestDto();
        urlPrivee.setUrl("http://10.0.0.1/api");
        urlPrivee.setToken("token");
        urlPrivee.setSiret("12345678901234");
        assertThat(service.testSirene(urlPrivee).getResult()).isEqualTo("error");
    }

    @Test
    void testDocaposteValideChaqueParametre() {
        assertThat(service.testDocaposte(null).getMessage()).contains("URI");

        DocaposteTestRequestDto sansSiren = new DocaposteTestRequestDto();
        sansSiren.setUri("https://docaposte.fr");
        assertThat(service.testDocaposte(sansSiren).getMessage()).contains("SIREN");

        DocaposteTestRequestDto sansKeystore = new DocaposteTestRequestDto();
        sansKeystore.setUri("https://docaposte.fr");
        sansKeystore.setSiren("123456789");
        assertThat(service.testDocaposte(sansKeystore).getMessage()).contains("Keystore");

        DocaposteTestRequestDto sansTruststore = new DocaposteTestRequestDto();
        sansTruststore.setUri("https://docaposte.fr");
        sansTruststore.setSiren("123456789");
        sansTruststore.setKeystorePath("/tmp/ks.p12");
        sansTruststore.setKeystorePassword("secret");
        assertThat(service.testDocaposte(sansTruststore).getMessage()).contains("Truststore");
    }

    @Test
    void testEsupSignatureValideLesParametresPuisPingLUrl() {
        assertThat(service.testEsupSignature(null).getMessage()).contains("URI");

        EsupSignatureTestRequestDto sansCircuit = new EsupSignatureTestRequestDto();
        sansCircuit.setUri("https://esup-signature.fr");
        assertThat(service.testEsupSignature(sansCircuit).getMessage()).contains("Circuit");

        EsupSignatureTestRequestDto urlLocale = new EsupSignatureTestRequestDto();
        urlLocale.setUri("http://127.0.0.1/api");
        urlLocale.setCircuit("circuit-1");
        assertThat(service.testEsupSignature(urlLocale).getResult()).isEqualTo("error");
    }
}
