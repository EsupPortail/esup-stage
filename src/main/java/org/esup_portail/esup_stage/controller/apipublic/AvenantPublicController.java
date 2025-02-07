package org.esup_portail.esup_stage.controller.apipublic;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.esup_portail.esup_stage.controller.apipublic.response.GenericResponse;
import org.esup_portail.esup_stage.dto.MetadataDto;
import org.esup_portail.esup_stage.dto.PdfMetadataDto;
import org.esup_portail.esup_stage.dto.UpdateDatesRequest;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.CentreGestionSignataire;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.service.signature.SignatureService;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@ApiPublicController
@RequestMapping("/avenants")
public class AvenantPublicController {

    @Autowired
    AvenantJpaRepository avenantJpaRepository;

    @Autowired
    SignatureService signatureService;

    @GetMapping(value = "/{id}/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Récupération des metadata de l'avenant")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avenant non trouvé",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}
            )
    })
    public MetadataDto getAvenantMetadata(
            @PathVariable("id") @Parameter(description = "Identifiant de l'avenant") int id
    ) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant inexistant");
        }
        return signatureService.getPublicMetadata(avenant.getConvention(), avenant.getId());
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(description = "Récupération du PDF de l'avenant")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avenant non trouvé",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}
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
                .body(signatureService.getPublicPdf(avenant.getConvention(), avenant));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Récupération du PDF de l'avenant en base64 et des metadata")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avenant non trouvé",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}
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
        pdfMetadataDto.setPdf64(Base64.getEncoder().encodeToString(signatureService.getPublicPdf(avenant.getConvention(), avenant)));
        pdfMetadataDto.setMetadata(signatureService.getPublicMetadata(avenant.getConvention(), avenant.getId()));
        return pdfMetadataDto;
    }

    @PatchMapping(value = "/{id}/dates", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Mise à jour des dates de dépôt et de signature")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avenant non trouvé",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}
            )
    })
    public GenericResponse updateDates(
            @PathVariable("id") @Parameter(description = "Identifiant de l'avenant") int id,
            @RequestBody List<UpdateDatesRequest> request
    ) {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant inexistant");
        }
        List<CentreGestionSignataire> signataires = avenant.getConvention().getCentreGestion().getSignataires();
        List<Historique> historiques = new ArrayList<>();
        for (UpdateDatesRequest req : request) {
            CentreGestionSignataire signataire = signataires.stream().filter(s -> s.getOrdre() == req.getOrder()).findAny().orElse(null);
            if (signataire == null) {
                continue;
            }
            Historique historique = new Historique();
            historique.setDateSignature(req.getSignatureDate());
            historique.setDateDepot(req.getSubmissionDate());
            historique.setTypeSignataire(signataire.getId().getSignataire());
            historiques.add(historique);
        }
        signatureService.setSignatureHistorique(avenant, historiques);
        return new GenericResponse();
    }

    @PostMapping(value = "/{id}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Dépôt du PDF de l'avenant signé")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avenant non trouvé",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}
            )
    })
    public GenericResponse uploadPdf(
            @PathVariable("id") @Parameter(description = "Identifiant de l'avenant") int id,
            @RequestParam("doc") MultipartFile doc
    ) throws IOException {
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant inexistant");
        }

        MetadataDto metadataDto = signatureService.getPublicMetadata(avenant.getConvention(), avenant.getId());
        signatureService.saveSignedFile(metadataDto, doc.getInputStream());

        return new GenericResponse();
    }
}
