package org.esup_portail.esup_stage.service.signature;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.docaposte.DocaposteClient;
import org.esup_portail.esup_stage.dto.MetadataDto;
import org.esup_portail.esup_stage.enums.AppSignatureEnum;
import org.esup_portail.esup_stage.enums.FolderEnum;
import org.esup_portail.esup_stage.enums.SignataireEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.esup_portail.esup_stage.webhook.esupsignature.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SignatureServiceHistoriqueTest {

    private SignatureService service;
    private ConventionJpaRepository conventionJpaRepository;
    private AvenantJpaRepository avenantJpaRepository;
    private SignatureProperties signatureProperties;
    private DocaposteClient docaposteClient;
    private WebhookService webhookService;
    private SignatureClient signatureClient;
    private AppliProperties appliProperties;
    private LdapService ldapService;
    private ImpressionService impressionService;

    @BeforeEach
    void setUp() {
        service = new SignatureService(WebClient.builder());
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        avenantJpaRepository = mock(AvenantJpaRepository.class);
        signatureProperties = mock(SignatureProperties.class);
        docaposteClient = mock(DocaposteClient.class);
        webhookService = mock(WebhookService.class);
        signatureClient = mock(SignatureClient.class);
        appliProperties = mock(AppliProperties.class);
        ldapService = mock(LdapService.class);
        impressionService = mock(ImpressionService.class);
        ReflectionTestUtils.setField(service, "conventionJpaRepository", conventionJpaRepository);
        ReflectionTestUtils.setField(service, "avenantJpaRepository", avenantJpaRepository);
        ReflectionTestUtils.setField(service, "signatureProperties", signatureProperties);
        ReflectionTestUtils.setField(service, "docaposteClient", docaposteClient);
        ReflectionTestUtils.setField(service, "webhookService", webhookService);
        ReflectionTestUtils.setField(service, "signatureClient", signatureClient);
        ReflectionTestUtils.setField(service, "appliProperties", appliProperties);
        ReflectionTestUtils.setField(service, "ldapService", ldapService);
        ReflectionTestUtils.setField(service, "impressionService", impressionService);
    }

    private Convention conventionSignature() {
        Convention convention = new Convention();
        convention.setId(42);
        convention.setDocumentId("DOC42");
        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Durand");
        etudiant.setPrenom("Alice");
        convention.setEtudiant(etudiant);
        Etape etape = new Etape();
        etape.setLibelle("L3");
        convention.setEtape(etape);
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setCircuitSignature("WF-1");
        centreGestion.setSignataires(List.of());
        convention.setCentreGestion(centreGestion);
        return convention;
    }

    private Historique historique(SignataireEnum type, Date depot, Date signature) {
        Historique historique = new Historique();
        historique.setTypeSignataire(type);
        historique.setDateDepot(depot);
        historique.setDateSignature(signature);
        return historique;
    }

    // ------------------------------------------------------------------
    // updateHistorique(Convention)
    // ------------------------------------------------------------------

    @Test
    void updateHistoriqueConventionVerifieLesPrerequis() {
        assertThatThrownBy(() -> service.updateHistorique((Convention) null)).isInstanceOf(AppException.class);

        // rafraîchissement récent : trop tôt
        Convention recente = conventionSignature();
        recente.setDateActualisationSignature(new Date());
        assertThatThrownBy(() -> service.updateHistorique(recente))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("30 minutes");

        // signature non configurée
        when(signatureProperties.getAppSignatureType()).thenReturn(null);
        assertThatThrownBy(() -> service.updateHistorique(conventionSignature()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("configurée");
    }

    @Test
    void updateHistoriqueConventionInterrogeDocaposte() {
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.DOCAPOSTE);
        Convention convention = conventionSignature();
        when(docaposteClient.getHistorique(eq("DOC42"), anyList()))
                .thenReturn(List.of(historique(SignataireEnum.etudiant, null, new Date())));

        service.updateHistorique(convention);

        assertThat(convention.getDateSignatureEtudiant()).isNotNull();
        assertThat(convention.getDateDepotEtudiant()).as("repli sur la date de signature").isNotNull();
        verify(conventionJpaRepository).save(convention);
    }

    @Test
    void updateHistoriqueConventionInterrogeEsupSignatureOuLExterne() {
        // esup-signature
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.ESUPSIGNATURE);
        Convention convention = conventionSignature();
        when(webhookService.getHistorique(eq("DOC42"), eq(convention))).thenReturn(List.of());
        service.updateHistorique(convention);
        verify(conventionJpaRepository).save(convention);

        // application externe : l'historique est délégué, pas de sauvegarde ici
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.EXTERNE);
        Convention externe = conventionSignature();
        service.updateHistorique(externe);
        verify(webhookService).getHistoriqueExterne(externe);
        verify(conventionJpaRepository, never()).save(externe);
    }

    @Test
    void updateHistoriqueConventionTraduitLesPannes() {
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.DOCAPOSTE);
        when(docaposteClient.getHistorique(anyString(), anyList())).thenThrow(new RuntimeException("api down"));

        assertThatThrownBy(() -> service.updateHistorique(conventionSignature()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("historique");
    }

    // ------------------------------------------------------------------
    // updateHistorique(Avenant) + setSignatureHistorique(Avenant)
    // ------------------------------------------------------------------

    private Avenant avenantSignature() {
        Avenant avenant = new Avenant();
        avenant.setId(9);
        avenant.setDocumentId("DOC9");
        avenant.setConvention(conventionSignature());
        return avenant;
    }

    @Test
    void updateHistoriqueAvenantVerifieLesPrerequis() {
        assertThatThrownBy(() -> service.updateHistorique((Avenant) null)).isInstanceOf(AppException.class);

        Avenant recent = avenantSignature();
        recent.setDateActualisationSignature(new Date());
        assertThatThrownBy(() -> service.updateHistorique(recent))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("30 minutes");

        when(signatureProperties.getAppSignatureType()).thenReturn(null);
        assertThatThrownBy(() -> service.updateHistorique(avenantSignature()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("configurée");

        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.DOCAPOSTE);
        when(docaposteClient.getHistorique(anyString(), anyList())).thenThrow(new RuntimeException("api down"));
        assertThatThrownBy(() -> service.updateHistorique(avenantSignature()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("historique");
    }

    @Test
    void updateHistoriqueAvenantCouvreLesTroisApplications() {
        // Docaposte
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.DOCAPOSTE);
        Avenant avenant = avenantSignature();
        when(docaposteClient.getHistorique(eq("DOC9"), anyList()))
                .thenReturn(List.of(historique(SignataireEnum.enseignant, new Date(), new Date())));
        service.updateHistorique(avenant);
        assertThat(avenant.getDateSignatureEnseignant()).isNotNull();
        verify(avenantJpaRepository).save(avenant);

        // esup-signature
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.ESUPSIGNATURE);
        Avenant avenantEsup = avenantSignature();
        when(webhookService.getHistorique(eq("DOC9"), any(Convention.class))).thenReturn(List.of());
        service.updateHistorique(avenantEsup);

        // externe
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.EXTERNE);
        Avenant avenantExterne = avenantSignature();
        service.updateHistorique(avenantExterne);
        verify(webhookService).getHistoriqueExterne(avenantExterne);
    }

    @Test
    void lHistoriqueAvenantRemplitToutesLesDates(@TempDir Path tempDir) throws Exception {
        when(appliProperties.getDataDir()).thenReturn(tempDir.toString());
        Files.createDirectories(Paths.get(tempDir.toString() + FolderEnum.SIGNATURES));
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.DOCAPOSTE);

        Avenant avenant = avenantSignature();
        when(avenantJpaRepository.findById((Integer) 9)).thenReturn(Optional.of(avenant));
        when(docaposteClient.download("DOC9")).thenReturn(new ByteArrayInputStream(pdfValide()));
        Date depot = new Date();
        Date signatureDate = new Date();
        List<Historique> historiques = List.of(
                historique(SignataireEnum.etudiant, depot, signatureDate),
                historique(SignataireEnum.enseignant, null, signatureDate), // repli sur la date de signature
                historique(SignataireEnum.tuteur, depot, signatureDate),
                historique(SignataireEnum.signataire, depot, signatureDate),
                historique(SignataireEnum.viseur, depot, signatureDate));

        service.setSignatureHistorique(avenant, historiques);

        assertThat(avenant.getDateSignatureEtudiant()).isEqualTo(signatureDate);
        assertThat(avenant.getDateDepotEnseignant()).as("repli sur la date de signature").isEqualTo(signatureDate);
        assertThat(avenant.isAllSignedDateSetted()).isTrue();
        // toutes les dates posées : le PDF signé est téléchargé et archivé
        assertThat(Files.exists(Paths.get(service.getSignatureFilePath("Avenant_9_Durand_Alice")))).isTrue();
    }

    // ------------------------------------------------------------------
    // téléchargement et validation du PDF signé
    // ------------------------------------------------------------------

    private byte[] pdfValide() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(out));
        pdf.addNewPage();
        pdf.close();
        return out.toByteArray();
    }

    @Test
    void downloadSignedPdfEcritLeFichierDocaposte(@TempDir Path tempDir) throws Exception {
        when(appliProperties.getDataDir()).thenReturn(tempDir.toString());
        Files.createDirectories(Paths.get(tempDir.toString() + FolderEnum.SIGNATURES));
        MetadataDto metadataDto = mock(MetadataDto.class);
        when(metadataDto.getTitle()).thenReturn("Convention_42");

        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.DOCAPOSTE);
        when(docaposteClient.download("DOC42")).thenReturn(new ByteArrayInputStream(pdfValide()));
        service.downloadSignedPdf("DOC42", metadataDto);
        assertThat(Files.exists(Paths.get(service.getSignatureFilePath("Convention_42")))).isTrue();

        // esup-signature sans document : rien n'est écrit
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.ESUPSIGNATURE);
        when(webhookService.download("AUTRE")).thenReturn(null);
        assertThatCode(() -> service.downloadSignedPdf("AUTRE", metadataDto)).doesNotThrowAnyException();
    }

    @Test
    void unDocumentSigneInvalideEstRejete(@TempDir Path tempDir) throws Exception {
        when(appliProperties.getDataDir()).thenReturn(tempDir.toString());
        Files.createDirectories(Paths.get(tempDir.toString() + FolderEnum.SIGNATURES));
        MetadataDto metadataDto = mock(MetadataDto.class);
        when(metadataDto.getTitle()).thenReturn("Convention_43");

        // pas un PDF du tout
        assertThatThrownBy(() -> service.saveSignedFile(metadataDto, new ByteArrayInputStream("bonjour".getBytes())))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("PDF valide");

        // entête PDF mais contenu corrompu
        assertThatThrownBy(() -> service.saveSignedFile(metadataDto, new ByteArrayInputStream("%PDF-corrompu".getBytes())))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("PDF valide");
    }

    // ------------------------------------------------------------------
    // mise à jour automatique planifiée
    // ------------------------------------------------------------------

    @Test
    void updateInterrogeLeClientSelonLApplication() {
        // Docaposte via le client de signature générique
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.DOCAPOSTE);
        Convention convention = conventionSignature();
        when(signatureClient.getHistorique(eq("DOC42"), anyList()))
                .thenReturn(List.of(historique(SignataireEnum.tuteur, new Date(), new Date())));
        service.update(convention);
        assertThat(convention.getDateSignatureTuteur()).isNotNull();

        // esup-signature via le statut
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.ESUPSIGNATURE);
        Convention esup = conventionSignature();
        when(webhookService.getHistoriqueStatus(eq("DOC42"), eq(esup))).thenReturn(List.of());
        service.update(esup);
        verify(conventionJpaRepository).save(esup);

        // externe : délégué au webhook
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.EXTERNE);
        Convention externe = conventionSignature();
        service.update(externe);
        verify(webhookService).getHistoriqueExterne(externe);
    }

    @Test
    void updateAutoContinueMalgreLesErreurs() {
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.DOCAPOSTE);
        Convention enErreur = conventionSignature();
        Convention correcte = conventionSignature();
        correcte.setDocumentId("DOC43");
        when(conventionJpaRepository.findConventionNonSignees()).thenReturn(List.of(enErreur, correcte));
        when(signatureClient.getHistorique(eq("DOC42"), anyList())).thenThrow(new RuntimeException("api down"));
        when(signatureClient.getHistorique(eq("DOC43"), anyList())).thenReturn(List.of());

        assertThatCode(() -> service.updateAuto()).doesNotThrowAnyException();
        verify(conventionJpaRepository).save(correcte);

        // aucune convention à traiter
        when(conventionJpaRepository.findConventionNonSignees()).thenReturn(null);
        assertThatCode(() -> service.updateAuto()).doesNotThrowAnyException();
    }
}
