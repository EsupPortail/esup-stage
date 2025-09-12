package org.esup_portail.esup_stage.service.signature;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.docaposte.DocaposteClient;
import org.esup_portail.esup_stage.dto.IdsListDto;
import org.esup_portail.esup_stage.dto.ResponseDto;
import org.esup_portail.esup_stage.enums.AppSignatureEnum;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.Etudiant;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConventionService;
import org.esup_portail.esup_stage.service.MailerService;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.webhook.esupsignature.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.*;

import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de SignatureService.upload(...) :
 * - Chemin DOCAPOSTE : appelle DocaposteClient.upload et retourne count=1
 * - Chemin ESUPSIGNATURE : POST -> documentId, update convention + delete ancien documentId
 */
class SignatureServiceUploadTest {

    // SUT
    private SignatureService service;

    // Mocks dépendances
    private ConventionJpaRepository conventionRepo;
    private AvenantJpaRepository avenantRepo;
    private CentreGestionJpaRepository centreGestionRepo;
    private ConventionService conventionService;
    private ImpressionService impressionService;
    private WebhookService webhookService;
    private SignatureProperties signatureProperties;
    private AppliProperties appliProperties;
    private SignatureClient signatureClient;
    private LdapService ldapService;
    private MailerService mailerService;
    private AppConfigService appConfigService;
    private DocaposteClient docaposteClient;

    // WebClient (réel) avec ExchangeFunction stub
    private WebClient.Builder webClientBuilder;

    // Pour introspection des requêtes envoyées via WebClient pendant le test ESUPSIGNATURE
    private final List<String> httpTrace = new CopyOnWriteArrayList<>();

    @BeforeEach
    void setup() {
        // Mocks
        conventionRepo = mock(ConventionJpaRepository.class);
        avenantRepo = mock(AvenantJpaRepository.class);
        centreGestionRepo = mock(CentreGestionJpaRepository.class);
        conventionService = mock(ConventionService.class);
        impressionService = mock(ImpressionService.class);
        webhookService = mock(WebhookService.class);
        signatureProperties = mock(SignatureProperties.class, RETURNS_DEEP_STUBS);
        appliProperties = mock(AppliProperties.class, RETURNS_DEEP_STUBS);
        signatureClient = mock(SignatureClient.class);
        ldapService = mock(LdapService.class);
        mailerService = mock(MailerService.class);
        appConfigService = mock(AppConfigService.class, RETURNS_DEEP_STUBS);
        docaposteClient = mock(DocaposteClient.class);

        // Pré-checks OK
        ResponseDto prechecksOk = mock(ResponseDto.class);
        when(prechecksOk.getError()).thenReturn(Collections.emptyList());
        when(conventionService.controleEmailTelephone(any(Convention.class))).thenReturn(prechecksOk);

        // WebClient avec ExchangeFunction maison (POST -> "DOC-ESUP-001", DELETE -> 200)
        ExchangeFunction fx = request -> {
            String method = request.method().name();
            URI url = request.url();
            httpTrace.add(method + " " + url);

            if ("POST".equals(method)) {
                // Retourne un body "DOC-ESUP-001"
                String body = "DOC-ESUP-001";
                ClientResponse resp = ClientResponse
                        .create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .body("DOC-ESUP-001")
                        .build();
                return Mono.just(resp);
            } else if ("DELETE".equals(method)) {
                ClientResponse resp = ClientResponse
                        .create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                        .body("")
                        .build();
                return Mono.just(resp);
            }
            // Par défaut 404 pour méthodes non attendues
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        };
        webClientBuilder = WebClient.builder().exchangeFunction(fx);

        // Service sous test
        service = new SignatureService(webClientBuilder);

        // Injection des mocks (champs @Autowired privés)
        ReflectionTestUtils.setField(service, "conventionJpaRepository", conventionRepo);
        ReflectionTestUtils.setField(service, "avenantJpaRepository", avenantRepo);
        ReflectionTestUtils.setField(service, "centreGestionJpaRepository", centreGestionRepo);
        ReflectionTestUtils.setField(service, "conventionService", conventionService);
        ReflectionTestUtils.setField(service, "impressionService", impressionService);
        ReflectionTestUtils.setField(service, "webhookService", webhookService);
        ReflectionTestUtils.setField(service, "signatureProperties", signatureProperties);
        ReflectionTestUtils.setField(service, "appliProperties", appliProperties);
        ReflectionTestUtils.setField(service, "signatureClient", signatureClient);
        ReflectionTestUtils.setField(service, "ldapService", ldapService);
        ReflectionTestUtils.setField(service, "utilisateurJpaRepository", mock(org.esup_portail.esup_stage.repository.UtilisateurJpaRepository.class));
        ReflectionTestUtils.setField(service, "mailerService", mailerService);
        ReflectionTestUtils.setField(service, "appConfigService", appConfigService);
        ReflectionTestUtils.setField(service, "docaposteClient", docaposteClient);
    }

    // --------- Helpers de fabrication d’entités minimales ---------

    private Convention makeConvention(int id, boolean envoiDocSigne, String workflowId) {
        Convention c = new Convention();
        c.setId(id);
        Etudiant etu = new Etudiant();
        etu.setPrenom("Alice");
        etu.setNom("Durand");
        c.setEtudiant(etu);
        CentreGestion cg = new CentreGestion();
        cg.setCircuitSignature(workflowId);
        cg.setEnvoiDocumentSigne(envoiDocSigne);
        c.setCentreGestion(cg);
        return c;
    }

    private IdsListDto makeIdsDto(int... ids) {
        IdsListDto dto = mock(IdsListDto.class);
        List<Integer> list = new ArrayList<>();
        for (int id : ids) list.add(id);
        when(dto.getIds()).thenReturn(list);
        return dto;
    }

    // ---------------------- TESTS ----------------------

    @Test
    void upload_Docaposte() {
        // SignatureProperties
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.DOCAPOSTE);

        // Convention retournée par le repo
        Convention conv = makeConvention(1, false, "WF-001");
        when(conventionRepo.findById(1)).thenReturn(conv);

        // DocaposteClient: simule la mise à jour du documentId
        doAnswer(inv -> {
            Convention cArg = inv.getArgument(0);
            cArg.setDocumentId("DOC-DP-001");
            cArg.setDateEnvoiSignature(new Date());
            return null;
        }).when(docaposteClient).upload(eq(conv), isNull());

        // Exécute
        int count = service.upload(makeIdsDto(1), false);

        // Vérifs
        assertThat(count).isEqualTo(1);
        verify(conventionService).controleEmailTelephone(conv);
        verify(docaposteClient, times(1)).upload(eq(conv), isNull());
        // Pas d'appel WebClient dans ce chemin
        assertThat(httpTrace).isEmpty();
    }

    @Test
    void upload_EsupSignature() {
        // SignatureProperties pour ESUPSIGNATURE
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.ESUPSIGNATURE);
        when(signatureProperties.getWebhook().getUri()).thenReturn("http://webhook/esup/create");
        when(signatureProperties.getWebhook().getToken()).thenReturn("TOKEN123");
        when(signatureProperties.getEsupsignature().getUri()).thenReturn("http://esup-signature");

        // Convention avec un documentId existant -> doit déclencher le DELETE
        Convention conv = makeConvention(2, false, "WF-002");
        conv.setDocumentId("OLD-123");
        when(conventionRepo.findById(2)).thenReturn(conv);

        // Act
        int count = service.upload(makeIdsDto(2), false);

        // Assert
        assertThat(count).isEqualTo(1);

        // La convention doit être mise à jour avec le nouveau documentId et persistée
        ArgumentCaptor<Convention> saved = ArgumentCaptor.forClass(Convention.class);
        verify(conventionRepo, atLeastOnce()).saveAndFlush(saved.capture());
        Convention savedConv = saved.getValue();
        assertThat(savedConv.getDocumentId()).isEqualTo("DOC-ESUP-001");
        assertThat(savedConv.getDateEnvoiSignature()).isNotNull();

        // Trafic HTTP attendu : POST (création) puis DELETE (ancien)
        assertThat(httpTrace).anyMatch(s -> s.startsWith("POST ") && s.contains("http://webhook/esup/create") && s.contains("conventionid=2"));
        assertThat(httpTrace).anyMatch(s -> s.startsWith("DELETE ") && s.contains("http://esup-signature/signrequests/soft/OLD-123"));

        // Contrôles amont effectués
        verify(conventionService).controleEmailTelephone(conv);
    }
}
