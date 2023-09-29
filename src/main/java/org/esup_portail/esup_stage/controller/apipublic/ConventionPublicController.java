package org.esup_portail.esup_stage.controller.apipublic;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.esup_portail.esup_stage.dto.MetadataDto;
import org.esup_portail.esup_stage.dto.MetadataSignataireDto;
import org.esup_portail.esup_stage.dto.PdfMetadataDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.service.ConventionService;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@ApiPublicController
@RequestMapping("/conventions")
public class ConventionPublicController {

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    ImpressionService impressionService;

    @Autowired
    ConventionService conventionService;

    @GetMapping(value = "/{id}/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Récupération des metadata de la convention")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Convention non trouvée",
                    content = { @Content(mediaType = MediaType.TEXT_PLAIN_VALUE) }
            )
    })
    public MetadataDto getConventionMetadata(
            @PathVariable("id") @Parameter(description = "Identifiant de la convention") int id
    ) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention inexistante");
        }
        return getMetadata(convention);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(description = "Récupération du PDF de la convention")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Convention non trouvée",
                    content = { @Content(mediaType = MediaType.TEXT_PLAIN_VALUE) }
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
                .body(getPdf(convention));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Récupération du PDF de la convention en base64 et des metadata")
    @ApiResponses({
            @ApiResponse(),
            @ApiResponse(
                    responseCode = "404",
                    description = "Convention non trouvée",
                    content = { @Content(mediaType = MediaType.TEXT_PLAIN_VALUE) }
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
        pdfMetadataDto.setPdf64(Base64.getEncoder().encodeToString(getPdf(convention)));
        pdfMetadataDto.setMetadata(getMetadata(convention));
        return pdfMetadataDto;
    }

    private byte[] getPdf(Convention convention) {
        if (convention.getNomEtabRef() == null || convention.getAdresseEtabRef() == null) {
            CentreGestion centreGestionEtab = centreGestionJpaRepository.getCentreEtablissement();
            if (centreGestionEtab == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Centre de gestion de type établissement non trouvé");
            }
            convention.setNomEtabRef(centreGestionEtab.getNomCentre());
            convention.setAdresseEtabRef(centreGestionEtab.getAdresseComplete());
            conventionJpaRepository.saveAndFlush(convention);
        }
        ByteArrayOutputStream ou = new ByteArrayOutputStream();
        impressionService.generateConventionAvenantPDF(convention, null, ou, false);

        return ou.toByteArray();
    }

    private MetadataDto getMetadata(Convention convention) {
        MetadataDto metadata = new MetadataDto();
        metadata.setTitle("Convention_" + convention.getId() + "_" + convention.getEtudiant().getNom() + "_" + convention.getEtudiant().getPrenom());
        metadata.setCompanyname(convention.getNomEtabRef());
        metadata.setSchool(convention.getEtape().getLibelle());
        List<MetadataSignataireDto> signataires = new ArrayList<>();

        convention.getCentreGestion().getSignataires().forEach(s -> {
            MetadataSignataireDto signataireDto = new MetadataSignataireDto();
            String phone = "";
            switch (s.getId().getSignataire()) {
                case etudiant:
                    Etudiant etudiant = convention.getEtudiant();
                    signataireDto.setName(etudiant.getNom());
                    signataireDto.setGivenname(etudiant.getPrenom());
                    signataireDto.setMail(etudiant.getMail());
                    phone = convention.getTelPortableEtudiant();
                    signataireDto.setOrder(s.getOrdre());
                    break;
                case enseignant:
                    Enseignant enseignant = convention.getEnseignant();
                    signataireDto.setName(enseignant.getNom());
                    signataireDto.setGivenname(enseignant.getPrenom());
                    signataireDto.setMail(enseignant.getMail());
                    phone = enseignant.getTel();
                    signataireDto.setOrder(s.getOrdre());
                    break;
                case tuteur:
                    Contact tuteur = convention.getContact();
                    signataireDto.setName(tuteur.getNom());
                    signataireDto.setGivenname(tuteur.getPrenom());
                    signataireDto.setMail(tuteur.getMail());
                    phone = tuteur.getTel();
                    signataireDto.setOrder(s.getOrdre());
                    break;
                case signataire:
                    Contact signataire = convention.getSignataire();
                    signataireDto.setName(signataire.getNom());
                    signataireDto.setGivenname(signataire.getPrenom());
                    signataireDto.setMail(signataire.getMail());
                    phone = signataire.getTel();
                    signataireDto.setOrder(s.getOrdre());
                    break;
                case viseur:
                    CentreGestion centreGestion = convention.getCentreGestion();
                    signataireDto.setName(centreGestion.getNomViseur());
                    signataireDto.setGivenname(centreGestion.getPrenomViseur());
                    signataireDto.setMail(centreGestion.getMail());
                    phone = centreGestion.getTelephone();
                    signataireDto.setOrder(s.getOrdre());
                    break;
            }
            if (signataireDto.getOrder() != 0) {
                signataireDto.setPhone(conventionService.parseNumTel(phone));
                signataires.add(signataireDto);
            }
        });

        metadata.setSignatory(signataires);
        return metadata;
    }
}
