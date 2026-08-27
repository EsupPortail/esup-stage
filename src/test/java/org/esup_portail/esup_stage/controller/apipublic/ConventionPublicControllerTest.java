package org.esup_portail.esup_stage.controller.apipublic;

import org.esup_portail.esup_stage.dto.MetadataDto;
import org.esup_portail.esup_stage.dto.PdfMetadataDto;
import org.esup_portail.esup_stage.dto.UpdateDatesRequest;
import org.esup_portail.esup_stage.enums.SignataireEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.CentreGestionSignataire;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.service.signature.SignatureService;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConventionPublicControllerTest {

    private ConventionPublicController controller;
    private ConventionJpaRepository conventionJpaRepository;
    private SignatureService signatureService;

    @BeforeEach
    void setUp() {
        controller = new ConventionPublicController();
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        signatureService = mock(SignatureService.class);
        controller.conventionJpaRepository = conventionJpaRepository;
        controller.signatureService = signatureService;
    }

    private Convention convention() {
        Convention convention = new Convention();
        convention.setId(42);
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(3);
        centreGestion.setSignataires(List.of(
                new CentreGestionSignataire(centreGestion, SignataireEnum.etudiant, 1),
                new CentreGestionSignataire(centreGestion, SignataireEnum.tuteur, 2)));
        convention.setCentreGestion(centreGestion);
        return convention;
    }

    @Test
    void lesMetadonneesSontRenvoyeesPourUneConventionExistante() {
        Convention convention = convention();
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        MetadataDto metadata = new MetadataDto();
        when(signatureService.getPublicMetadata(convention)).thenReturn(metadata);

        assertThat(controller.getConventionMetadata(42)).isSameAs(metadata);

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getConventionMetadata(99)).isInstanceOf(AppException.class);
    }

    @Test
    void lePdfEstServiEnPieceJointe() {
        Convention convention = convention();
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        when(signatureService.getPublicPdf(convention, null)).thenReturn("%PDF".getBytes());

        var reponse = controller.getConventionPdf(42);

        assertThat(reponse.getHeaders().getContentDisposition().getFilename()).isEqualTo("Convention_42.pdf");
        assertThat(reponse.getBody()).isEqualTo("%PDF".getBytes());

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getConventionPdf(99)).isInstanceOf(AppException.class);
    }

    @Test
    void leCombineRenvoiePdfBase64EtMetadata() {
        Convention convention = convention();
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        when(signatureService.getPublicPdf(convention, null)).thenReturn("%PDF".getBytes());
        when(signatureService.getPublicMetadata(convention)).thenReturn(new MetadataDto());

        PdfMetadataDto dto = controller.getConvention(42);

        assertThat(dto.getPdf64()).isEqualTo(Base64.getEncoder().encodeToString("%PDF".getBytes()));
        assertThat(dto.getMetadata()).isNotNull();
    }

    @Test
    void laMiseAJourDesDatesMappeLesSignatairesParOrdre() {
        Convention convention = convention();
        when(conventionJpaRepository.findById(42)).thenReturn(convention);

        UpdateDatesRequest etudiant = new UpdateDatesRequest();
        etudiant.setOrder(1);
        etudiant.setSignatureDate(new Date(1700000000000L));
        etudiant.setSubmissionDate(new Date(1699900000000L));
        UpdateDatesRequest ordreInconnu = new UpdateDatesRequest();
        ordreInconnu.setOrder(9);

        controller.updateDates(42, List.of(etudiant, ordreInconnu));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Historique>> captor = ArgumentCaptor.forClass(List.class);
        verify(signatureService).setSignatureHistorique(eq(convention), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getTypeSignataire()).isEqualTo(SignataireEnum.etudiant);

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.updateDates(99, List.of())).isInstanceOf(AppException.class);
    }

    @Test
    void leDepotDuPdfSigneEstTransmisAuServiceDeSignature() throws Exception {
        Convention convention = convention();
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        when(signatureService.getPublicMetadata(convention)).thenReturn(new MetadataDto());

        controller.uploadPdf(42, new MockMultipartFile("doc", "convention.pdf", "application/pdf", "%PDF".getBytes()));

        verify(signatureService).saveSignedFile(any(MetadataDto.class), any());

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.uploadPdf(99,
                new MockMultipartFile("doc", "x.pdf", "application/pdf", new byte[0])))
                .isInstanceOf(AppException.class);
    }
}
