package org.esup_portail.esup_stage.docaposte;

import jakarta.xml.bind.JAXBElement;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.docaposte.gen.*;
import org.esup_portail.esup_stage.enums.TypeSignatureEnum;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ws.client.core.WebServiceTemplate;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

class DocaposteClientUploadTest{

    private DocaposteClient client;
    private SignatureProperties signatureProperties;
    private AppliProperties appliProperties;
    private ImpressionService impressionService;
    private ConventionJpaRepository conventionRepo;
    private AvenantJpaRepository avenantRepo;
    private WebServiceTemplate webServiceTemplate;

    @BeforeEach
    void setUp() throws Exception {
        signatureProperties = mock(SignatureProperties.class, RETURNS_DEEP_STUBS);
        when(signatureProperties.getDocaposte().getSiren()).thenReturn("123456789");

        appliProperties = mock(AppliProperties.class, RETURNS_DEEP_STUBS);
        when(appliProperties.getMailer().getDeliveryAddress()).thenReturn(null);

        impressionService = mock(ImpressionService.class);
        conventionRepo = mock(ConventionJpaRepository.class);
        avenantRepo = mock(AvenantJpaRepository.class);

        webServiceTemplate = mock(WebServiceTemplate.class);
        when(webServiceTemplate.marshalSendAndReceive(any())).thenAnswer(inv -> {
            Object req = ((JAXBElement<?>) inv.getArgument(0));
            String local = ((JAXBElement<?>) req).getName().getLocalPart();
            ObjectFactory of = new ObjectFactory();
            return switch (local) {
                case "upload" -> {
                    UploadResponse r = new UploadResponse();
                    r.setReturn("DOC-12345");
                    yield of.createUploadResponse(r);
                }
                case "uploadMeta" -> of.createUploadMetaResponse(new UploadMetaResponse());
                case "uploadOTPInformation" -> of.createUploadOTPInformationResponse(new UploadOTPInformationResponse());
                default -> null;
            };
        });

        client = new DocaposteClient(signatureProperties);
        client.appliProperties = appliProperties;
        client.impressionService = impressionService;
        client.conventionJpaRepository = conventionRepo;
        client.avenantJpaRepository = avenantRepo;
        client.setWebServiceTemplate(webServiceTemplate);

        doAnswer(inv -> {
            ByteArrayOutputStream os = inv.getArgument(2);
            os.write("%PDF\n".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(impressionService).generateConventionAvenantPDF(any(), any(), any(), anyBoolean());

        when(impressionService.generateXmlData(any(Convention.class), eq(TypeSignatureEnum.serveur)))
                .thenReturn("<meta/>");
        when(impressionService.generateXmlData(any(Convention.class), eq(TypeSignatureEnum.otp)))
                .thenReturn("<otp/>");
    }

    @Test
    void upload_convention_ok() {
        Convention c = new Convention();
        c.setId(1);
        Etudiant etu = new Etudiant();
        etu.setPrenom("Alice"); etu.setNom("Durand");
        c.setEtudiant(etu);
        CentreGestion cg = new CentreGestion();
        cg.setCircuitSignature("WF-1");
        cg.setEnvoiDocumentSigne(false); // évite l’usage d’appliProperties mailer
        c.setCentreGestion(cg);

        client.upload(c, null);

        verify(webServiceTemplate, times(3)).marshalSendAndReceive(any());
        assertThat(c.getDocumentId()).isEqualTo("DOC-12345");
        assertThat(c.getDateEnvoiSignature()).isNotNull().isBeforeOrEqualsTo(new Date());

        ArgumentCaptor<Convention> cap = ArgumentCaptor.forClass(Convention.class);
        verify(conventionRepo).saveAndFlush(cap.capture());
        assertThat(cap.getValue().getDocumentId()).isEqualTo("DOC-12345");
    }
}
