package org.esup_portail.esup_stage.controller.apipublic;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.esup_portail.esup_stage.dto.MetadataDto;
import org.esup_portail.esup_stage.dto.PdfMetadataDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Base64;

@ApiPublicController
@RequestMapping("/avenants")
public class AvenantPublicController {

    @Autowired
    AvenantJpaRepository avenantJpaRepository;

    @Autowired
    ImpressionService impressionService;

    @GetMapping(value = "/{id}/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Récupération des metadata de l'avenant")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avenant non trouvé",
                    content = { @Content(mediaType = MediaType.TEXT_PLAIN_VALUE) }
            )
    })
    public MetadataDto getAvenantMetadata(
            @PathVariable("id") @Parameter(description = "Identifiant de l'avenant") int id
    ) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant inexistant");
        }
        return impressionService.getPublicMetadata(avenant.getConvention(), avenant.getId());
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(description = "Récupération du PDF de l'avenant")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avenant non trouvé",
                    content = { @Content(mediaType = MediaType.TEXT_PLAIN_VALUE) }
            )
    })
    public ResponseEntity<byte[]> getConventionPdf(
            @PathVariable("id") @Parameter(description = "Identifiant de l'avenant") int id
    ) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant inexistant");
        }
        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.builder("attachment")
                                .filename("Avenant_" + avenant.getId() + ".pdf")
                                .build()
                                .toString())
                .body(impressionService.getPublicPdf(avenant.getConvention(), avenant));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Récupération du PDF de l'avenant en base64 et des metadata")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avenant non trouvé",
                    content = { @Content(mediaType = MediaType.TEXT_PLAIN_VALUE) }
            )
    })
    public PdfMetadataDto getAvenant(
            @PathVariable("id") @Parameter(description = "Identifiant de l'avenant") int id
    ) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant inexistant");
        }

        PdfMetadataDto pdfMetadataDto = new PdfMetadataDto();
        pdfMetadataDto.setPdf64(Base64.getEncoder().encodeToString(impressionService.getPublicPdf(avenant.getConvention(), avenant)));
        pdfMetadataDto.setMetadata(impressionService.getPublicMetadata(avenant.getConvention(), avenant.getId()));
        return pdfMetadataDto;
    }
}
