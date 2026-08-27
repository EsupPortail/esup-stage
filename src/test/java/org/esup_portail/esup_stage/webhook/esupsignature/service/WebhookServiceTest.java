package org.esup_portail.esup_stage.webhook.esupsignature.service;

import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.config.properties.signature.DocaposteProperties;
import org.esup_portail.esup_stage.config.properties.signature.EsupSignatureProperties;
import org.esup_portail.esup_stage.config.properties.signature.WebhookProperties;
import org.esup_portail.esup_stage.enums.SignataireEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.CentreGestionSignataire;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests du client esup-signature : les échanges HTTP sont simulés par une
 * ExchangeFunction, aucun appel réseau réel.
 */
class WebhookServiceTest {

    private WebhookService serviceRepondant(HttpStatus status, String jsonBody) {
        ExchangeFunction exchange = request -> Mono.just(ClientResponse.create(status)
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .build());
        WebhookService service = new WebhookService(WebClient.builder().exchangeFunction(exchange));

        WebhookProperties webhook = new WebhookProperties();
        webhook.setUri("http://signature.test/api");
        webhook.setToken("jeton");
        EsupSignatureProperties esupSignature = new EsupSignatureProperties();
        esupSignature.setUri("http://signature.test/esup");
        SignatureProperties signatureProperties =
                new SignatureProperties(new DocaposteProperties(), webhook, esupSignature);
        ReflectionTestUtils.setField(service, "signatureProperties", signatureProperties);
        return service;
    }

    private Convention conventionAvecSignataires() {
        Convention convention = new Convention();
        convention.setId(42);
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(3);
        centreGestion.setSignataires(List.of(
                new CentreGestionSignataire(centreGestion, SignataireEnum.etudiant, 1),
                new CentreGestionSignataire(centreGestion, SignataireEnum.tuteur, 2)
        ));
        convention.setCentreGestion(centreGestion);
        return convention;
    }

    @Test
    void lHistoriqueEstMappeSurLesSignatairesParOrdre() {
        String auditTrail = "{\"id\":1,\"documentId\":9,\"auditSteps\":["
                + "{\"id\":1,\"email\":\"a@b.fr\",\"timeStampDate\":1700000000000},"
                + "{\"id\":2,\"email\":\"c@d.fr\",\"timeStampDate\":1700100000000}]}";
        WebhookService service = serviceRepondant(HttpStatus.OK, auditTrail);

        List<Historique> historiques = service.getHistorique("DOC-1", conventionAvecSignataires());

        assertThat(historiques).hasSize(2);
        assertThat(historiques.get(0).getTypeSignataire()).isEqualTo(SignataireEnum.etudiant);
        assertThat(historiques.get(0).getDateSignature()).isNotNull();
        assertThat(historiques.get(1).getTypeSignataire()).isEqualTo(SignataireEnum.tuteur);
    }

    @Test
    void lesEtapesSansSignataireCorrespondantSontIgnorees() {
        String auditTrail = "{\"id\":1,\"documentId\":9,\"auditSteps\":["
                + "{\"id\":1,\"timeStampDate\":1700000000000},"
                + "{\"id\":2,\"timeStampDate\":1700000000000},"
                + "{\"id\":3,\"timeStampDate\":1700000000000}]}";
        WebhookService service = serviceRepondant(HttpStatus.OK, auditTrail);

        List<Historique> historiques = service.getHistorique("DOC-1", conventionAvecSignataires());

        // 3 étapes mais seulement 2 signataires configurés
        assertThat(historiques).hasSize(2);
    }

    @Test
    void leStatutNeRetientQueLesActionsSignees() {
        String steps = "[{\"stepNumber\":1,\"recipientsActions\":["
                + "{\"actionType\":\"signed\",\"actionDate\":1700000000000},"
                + "{\"actionType\":\"viewed\",\"actionDate\":1700000000000}]},"
                + "{\"stepNumber\":2,\"recipientsActions\":[{\"actionType\":\"pending\"}]}]";
        WebhookService service = serviceRepondant(HttpStatus.OK, steps);

        List<Historique> historiques = service.getHistoriqueStatus("DOC-1", conventionAvecSignataires());

        assertThat(historiques).hasSize(1);
        assertThat(historiques.get(0).getTypeSignataire()).isEqualTo(SignataireEnum.etudiant);
    }

    @Test
    void leStatutExigeUnIdentifiantDeDocument() {
        WebhookService service = serviceRepondant(HttpStatus.OK, "[]");

        assertThatThrownBy(() -> service.getHistoriqueStatus(null, conventionAvecSignataires()))
                .isInstanceOf(AppException.class);
        assertThatThrownBy(() -> service.getHistoriqueStatus("null", conventionAvecSignataires()))
                .isInstanceOf(AppException.class);
    }

    @Test
    void leRefreshExterneTraduitLesErreursHttp() {
        assertThatCode(() -> serviceRepondant(HttpStatus.OK, "\"ok\"")
                .getHistoriqueExterne(conventionAvecSignataires())).doesNotThrowAnyException();

        assertAppException(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        assertAppException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN);
        assertAppException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND);
        assertAppException(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        assertAppException(HttpStatus.BAD_GATEWAY, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void assertAppException(HttpStatus reponse, HttpStatus attendu) {
        WebhookService service = serviceRepondant(reponse, "");

        assertThatThrownBy(() -> service.getHistoriqueExterne(conventionAvecSignataires()))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(attendu));
    }

    @Test
    void leRefreshExterneDUnAvenantSuitLesMemesRegles() {
        org.esup_portail.esup_stage.model.Avenant avenant = new org.esup_portail.esup_stage.model.Avenant();
        avenant.setId(9);

        assertThatCode(() -> serviceRepondant(HttpStatus.OK, "\"ok\"").getHistoriqueExterne(avenant))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> serviceRepondant(HttpStatus.NOT_FOUND, "").getHistoriqueExterne(avenant))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ------------------------------------------------------------------
    // upload / download
    // ------------------------------------------------------------------

    private org.esup_portail.esup_stage.dto.MetadataSignataireDto signataire(int ordre, String mail) {
        org.esup_portail.esup_stage.dto.MetadataSignataireDto dto = new org.esup_portail.esup_stage.dto.MetadataSignataireDto();
        dto.setOrder(ordre);
        dto.setMail(mail);
        dto.setName("Nom" + ordre);
        dto.setGivenname("Prenom" + ordre);
        return dto;
    }

    @Test
    void uploadConstruitLeCircuitEtEnvoieLeDocument() {
        WebhookService service = serviceRepondant(HttpStatus.OK, "\"WF-42\"");
        service.filenameSanitizerService = new org.esup_portail.esup_stage.service.FilenameSanitizerService();

        org.esup_portail.esup_stage.dto.MetadataDto metadata = new org.esup_portail.esup_stage.dto.MetadataDto();
        metadata.setTitle("Convention_42_Durand_Alice");
        metadata.setCompanyname("ACME");
        metadata.setSchool("L3");
        metadata.setWorkflowId("7");
        // deux signataires sur la même étape + un troisième : couvre la fusion des étapes
        metadata.setSignatory(new java.util.ArrayList<>(List.of(
                signataire(1, "alice@univ.fr"),
                signataire(1, "co-signataire@univ.fr"),
                signataire(2, "tuteur@acme.fr"))));
        metadata.setWatchers(List.of(new org.esup_portail.esup_stage.dto.MetadataObservateurDto("observateur@univ.fr")));

        org.esup_portail.esup_stage.dto.PdfMetadataDto contenu = new org.esup_portail.esup_stage.dto.PdfMetadataDto();
        contenu.setMetadata(metadata);
        contenu.setPdf64(com.itextpdf.commons.utils.Base64.encodeBytes("%PDF-1.4 test".getBytes()));

        assertThat(service.upload(contenu)).isEqualTo("\"WF-42\"");
    }

    @Test
    void downloadRecupereLeFluxDuDocumentSigne() throws Exception {
        WebhookService service = serviceRepondant(HttpStatus.OK, "CONTENU-PDF");

        java.io.InputStream flux = service.download("DOC42");

        assertThat(new String(flux.readAllBytes())).isEqualTo("CONTENU-PDF");
    }
}
