package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConventionDocumentsResponseDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.ConventionDocumentEtudiantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@ApiController
@RequestMapping("/conventions/{idConvention}/documents-etudiant")
public class ConventionDocumentEtudiantController {

    @Autowired
    private ConventionDocumentEtudiantService conventionDocumentEtudiantService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ConventionDocumentsResponseDto list(@PathVariable("idConvention") int idConvention) {
        return conventionDocumentEtudiantService.list(idConvention);
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public ConventionDocumentsResponseDto addDocument(
            @PathVariable("idConvention") int idConvention,
            @RequestParam(value = "doc") MultipartFile doc,
            @RequestParam(value = "remplacer", defaultValue = "false") boolean remplacer) {
        return conventionDocumentEtudiantService.addDocument(idConvention, doc, remplacer);
    }

    @GetMapping("/{idDocument}/preview")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> preview(@PathVariable("idConvention") int idConvention, @PathVariable("idDocument") int idDocument) {
        return conventionDocumentEtudiantService.preview(idConvention, idDocument);
    }

    @GetMapping("/{idDocument}/download")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> download(@PathVariable("idConvention") int idConvention, @PathVariable("idDocument") int idDocument) {
        return conventionDocumentEtudiantService.download(idConvention, idDocument);
    }

    @DeleteMapping("/{idDocument}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.SUPPRESSION, DroitEnum.MODIFICATION})
    public ConventionDocumentsResponseDto deleteDocument(@PathVariable("idConvention") int idConvention, @PathVariable("idDocument") int idDocument) {
        return conventionDocumentEtudiantService.deleteDocument(idConvention, idDocument);
    }
}