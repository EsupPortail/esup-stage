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
import org.esup_portail.esup_stage.model.CentreGestionSignataire;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
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
@RequestMapping("/conventions")
public class ConventionPublicController {

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    SignatureService signatureService;

    @GetMapping(value = "/{id}/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Récupération des metadata de la convention")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Convention non trouvée",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}
            )
    })
    public MetadataDto getConventionMetadata(
            @PathVariable("id") @Parameter(description = "Identifiant de la convention") int id
    ) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention inexistante");
        }
        return signatureService.getPublicMetadata(convention);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(description = "Récupération du PDF de la convention")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Convention non trouvée",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}
            )
    })
    public ResponseEntity<byte[]> getConventionPdf(
            @PathVariable("id") @Parameter(description = "Identifiant de la convention") int id
    ) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention inexistante");
        }
        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.builder("attachment")
                                .filename("Convention_" + convention.getId() + ".pdf")
                                .build()
                                .toString())
                .body(signatureService.getPublicPdf(convention, null));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Récupération du PDF de la convention en base64 et des metadata")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Convention non trouvée",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}
            )
    })
    public PdfMetadataDto getConvention(
            @PathVariable("id") @Parameter(description = "Identifiant de la convention") int id
    ) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention inexistante");
        }

        PdfMetadataDto pdfMetadataDto = new PdfMetadataDto();
        pdfMetadataDto.setPdf64(Base64.getEncoder().encodeToString(signatureService.getPublicPdf(convention, null)));
        pdfMetadataDto.setMetadata(signatureService.getPublicMetadata(convention));
        return pdfMetadataDto;
    }

    @PatchMapping(value = "/{id}/dates", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Mise à jour des dates de dépôt et de signature")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Convention non trouvée",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}
            )
    })
    public GenericResponse updateDates(
            @PathVariable("id") @Parameter(description = "Identifiant de la convention") int id,
            @RequestBody List<UpdateDatesRequest> request
    ) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention inexistante");
        }
        List<CentreGestionSignataire> signataires = convention.getCentreGestion().getSignataires();
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
        signatureService.setSignatureHistorique(convention, historiques);
        return new GenericResponse();
    }

    @PostMapping(value = "/{id}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Dépôt du PDF de la convention signée")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Convention non trouvée",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}
            )
    })
    public GenericResponse uploadPdf(
            @PathVariable("id") @Parameter(description = "Identifiant de la convention") int id,
            @RequestParam("doc") MultipartFile doc
    ) throws IOException {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention inexistante");
        }

        MetadataDto metadataDto = signatureService.getPublicMetadata(convention);
        signatureService.saveSignedFile(metadataDto, doc.getInputStream());

        return new GenericResponse();
    }
}
