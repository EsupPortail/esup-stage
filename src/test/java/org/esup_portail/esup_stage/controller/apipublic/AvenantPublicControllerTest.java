package org.esup_portail.esup_stage.controller.apipublic;

import org.esup_portail.esup_stage.dto.MetadataDto;
import org.esup_portail.esup_stage.dto.PdfMetadataDto;
import org.esup_portail.esup_stage.dto.UpdateDatesRequest;
import org.esup_portail.esup_stage.enums.SignataireEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.service.signature.SignatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvenantPublicControllerTest {

    private AvenantPublicController controller;
    private AvenantJpaRepository avenantJpaRepository;
    private SignatureService signatureService;

    @BeforeEach
    void setUp() {
        controller = new AvenantPublicController();
        avenantJpaRepository = mock(AvenantJpaRepository.class);
        signatureService = mock(SignatureService.class);
        controller.avenantJpaRepository = avenantJpaRepository;
        controller.signatureService = signatureService;
    }

    private Avenant avenant() {
        Avenant avenant = new Avenant();
        avenant.setId(9);
        Convention convention = new Convention();
        convention.setId(42);
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(3);
        centreGestion.setSignataires(List.of(
                new CentreGestionSignataire(centreGestion, SignataireEnum.etudiant, 1)));
        convention.setCentreGestion(centreGestion);
        avenant.setConvention(convention);
        return avenant;
    }

    @Test
    void metadataEtPdfDeLAvenant() {
        Avenant avenant = avenant();
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);
        MetadataDto metadata = new MetadataDto();
        when(signatureService.getPublicMetadata(avenant.getConvention(), 9)).thenReturn(metadata);
        when(signatureService.getPublicPdf(avenant.getConvention(), avenant)).thenReturn("%PDF".getBytes());

        assertThat(controller.getAvenantMetadata(9)).isSameAs(metadata);
        assertThat(controller.getConventionPdf(9).getBody()).isEqualTo("%PDF".getBytes());

        PdfMetadataDto dto = controller.getAvenant(9);
        assertThat(dto.getPdf64()).isEqualTo(Base64.getEncoder().encodeToString("%PDF".getBytes()));
        assertThat(dto.getMetadata()).isSameAs(metadata);
    }

    @Test
    void unAvenantInconnuEchouePartout() {
        when(avenantJpaRepository.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> controller.getAvenantMetadata(99)).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.getConventionPdf(99)).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.getAvenant(99)).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.updateDates(99, List.of())).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.uploadPdf(99,
                new MockMultipartFile("doc", "x.pdf", "application/pdf", new byte[0])))
                .isInstanceOf(AppException.class);
    }

    @Test
    void laMiseAJourDesDatesAlimenteLHistorique() {
        Avenant avenant = avenant();
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);

        UpdateDatesRequest requete = new UpdateDatesRequest();
        requete.setOrder(1);
        requete.setSignatureDate(new Date(1700000000000L));

        controller.updateDates(9, List.of(requete));

        verify(signatureService).setSignatureHistorique(eq(avenant), anyList());
    }

    @Test
    void leDepotDuPdfSigneEstTransmis() throws Exception {
        Avenant avenant = avenant();
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);
        when(signatureService.getPublicMetadata(avenant.getConvention(), 9)).thenReturn(new MetadataDto());

        controller.uploadPdf(9, new MockMultipartFile("doc", "avenant.pdf", "application/pdf", "%PDF".getBytes()));

        verify(signatureService).saveSignedFile(any(MetadataDto.class), any());
    }
}
