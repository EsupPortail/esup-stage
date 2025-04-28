package org.esup_portail.esup_stage.controller;

import jakarta.validation.Valid;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.ConsigneFormDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.enums.FolderEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Consigne;
import org.esup_portail.esup_stage.model.ConsigneDocument;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.ConsigneDocumentJpaRepository;
import org.esup_portail.esup_stage.repository.ConsigneJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.ColorConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@ApiController
@RequestMapping("/consignes")
public class ConsigneController {

    private static final Logger logger = LogManager.getLogger(ConsigneController.class);

    @Autowired
    AppliProperties appliProperties;

    @Autowired
    ConsigneJpaRepository consigneJpaRepository;

    @Autowired
    ConsigneDocumentJpaRepository consigneDocumentJpaRepository;
    @Autowired
    private CentreGestionJpaRepository centreGestionJpaRepository;

    @GetMapping("/centres/{idCentreGestion}")
    @Secure
    public Consigne getByCentreGestion(@PathVariable("idCentreGestion") int idCentreGestion) {
        return consigneJpaRepository.findByIdCentreGestion(idCentreGestion);
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL, AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.CREATION})
    public Consigne create(@Valid @RequestBody ConsigneFormDto consigneFormDto) {
        Consigne consigne = new Consigne();
        consigne.setCentreGestion(centreGestionJpaRepository.findById(consigneFormDto.getIdCentreGestion()));
        consigne.setTexte(ColorConverter.convertHslToRgb(consigneFormDto.getTexte()));
        consigne = consigneJpaRepository.saveAndFlush(consigne);
        return consigne;
    }

    @PatchMapping("/{idConsigne}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL, AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public Consigne update(@PathVariable("idConsigne") int idConsigne, @Valid @RequestBody ConsigneFormDto consigneFormDto) {
        Consigne consigne = consigneJpaRepository.findById(idConsigne);
        if (consigne == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Consigne non trouvée");
        }
        consigne.setTexte(ColorConverter.convertHslToRgb(consigneFormDto.getTexte()));
        consigne = consigneJpaRepository.saveAndFlush(consigne);
        return consigne;
    }

    @PostMapping("/{idConsigne}/add-doc")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL, AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public Consigne addDocument(@PathVariable("idConsigne") int idConsigne, @RequestParam(value="doc") MultipartFile doc) {
        Consigne consigne = consigneJpaRepository.findById(idConsigne);
        if (consigne == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Consigne non trouvée");
        }
        // Autorisation de l'upload de pdf, doc ou docx uniquement
        if (doc.getContentType() == null || (!doc.getContentType().equals("application/pdf") && !doc.getContentType().equals("application/msword") && !doc.getContentType().equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Le fichier doit être au format pdf, doc ou docx");
        }
        String extension = FilenameUtils.getExtension(doc.getOriginalFilename());
        String nomDocument = DigestUtils.md5Hex(doc.getOriginalFilename()) + "." + extension;
        String nomReel = doc.getOriginalFilename();

        ConsigneDocument consigneDocument = new ConsigneDocument();
        consigneDocument.setNom(nomDocument);
        consigneDocument.setNomReel(nomReel);
        consigne.addDocument(consigneDocument);
        consigneDocument = consigneDocumentJpaRepository.save(consigneDocument);
        consigne = consigneJpaRepository.saveAndFlush(consigne);

        try {
            String filename = this.getNomDocument(consigneDocument.getId(), consigneDocument.getNom());
            Path uploadLocation = Paths.get(this.getFilePath(filename));
            Files.copy(doc.getInputStream(), uploadLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du fichier", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'upload du fichier");
        }
        return consigne;
    }

    @DeleteMapping("/{idConsigne}/documents/{idDoc}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL, AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.SUPPRESSION})
    public Consigne deleteDoc(@PathVariable("idConsigne") int idConsigne, @PathVariable("idDoc") int idDoc) {
        ConsigneDocument consigneDocument = consigneDocumentJpaRepository.findById(idDoc);
        if (consigneDocument == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Document non trouvé");
        }
        Consigne consigne = consigneJpaRepository.findById(idConsigne);
        if (consigne == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Consigne non trouvée");
        }

        consigne.removeDoc(consigneDocument);
        consigne = consigneJpaRepository.saveAndFlush(consigne);

        try {
            Path filePath = Paths.get(getFilePath(getNomDocument(consigneDocument.getId(), consigneDocument.getNom())));
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            logger.error("Erreur lors de la suppression du fichier", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la suppression du fichier");
        }

        return consigne;
    }

    @GetMapping("/{idConsigne}/documents/{idDoc}/download")
    @Secure
    public ResponseEntity<byte[]> downloadDoc(@PathVariable("idConsigne") int idConsigne, @PathVariable("idDoc") int idDoc) {
        ConsigneDocument consigneDocument = consigneDocumentJpaRepository.findById(idDoc);
        if (consigneDocument == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Document non trouvé");
        }
        String filePath = getFilePath(getNomDocument(consigneDocument.getId(), consigneDocument.getNom()));
        if (Files.exists(Paths.get(filePath))) {
            try {
                return ResponseEntity.ok().body(FileUtils.readFileToByteArray(new File(filePath)));
            } catch (IOException e) {
                logger.error("Erreur lors de la lecture du fichier", e);
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur sur le téléchargement du fichier");
            }
        } else {
            throw new AppException(HttpStatus.NOT_FOUND, "Fichier non trouvé");
        }
    }

    private String getFilePath(String filename) {
        return appliProperties.getDataDir() + FolderEnum.CENTRE_GESTION_CONSIGNE_DOCS + "/" + filename;
    }

    private String getNomDocument(int idFichier, String nomFichier) {
        return idFichier + "_" + nomFichier;
    }
}
