package org.esup_portail.esup_stage.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.ConventionDocumentEtudiantDto;
import org.esup_portail.esup_stage.dto.ConventionDocumentsResponseDto;
import org.esup_portail.esup_stage.enums.FolderEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.ConventionDocumentEtudiant;
import org.esup_portail.esup_stage.model.ConventionDocumentEtudiantHistorique;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.ConventionDocumentEtudiantHistoriqueJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionDocumentEtudiantJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ConventionDocumentEtudiantService {

    private static final Logger logger = LogManager.getLogger(ConventionDocumentEtudiantService.class);
    private static final int DEFAULT_MAX_SIZE_MO = 10;

    @Autowired
    private ConventionJpaRepository conventionJpaRepository;

    @Autowired
    private ConventionDocumentEtudiantJpaRepository documentRepository;

    @Autowired
    private ConventionDocumentEtudiantHistoriqueJpaRepository historiqueRepository;

    @Autowired
    private ConventionService conventionService;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private AppliProperties appliProperties;

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private FilenameSanitizerService filenameSanitizerService;

    @Transactional(readOnly = true)
    public ConventionDocumentsResponseDto list(int idConvention) {
        Convention convention = getConventionForRead(idConvention);
        return buildResponse(convention);
    }

    @Transactional
    public ConventionDocumentsResponseDto addDocument(int idConvention, MultipartFile doc) {
        Convention convention = getConventionForWrite(idConvention);
        int maxSizeMo = getMaxSizeMo();
        FileValidationService.ValidatedPdf validatedPdf = fileValidationService.validatePdf(doc, maxSizeMo);

        String originalFilename = filenameSanitizerService.sanitize(doc.getOriginalFilename());
        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            originalFilename = originalFilename + ".pdf";
        }

        ConventionDocumentEtudiant document = new ConventionDocumentEtudiant();
        document.setConvention(convention);
        document.setNom(UUID.randomUUID() + ".pdf");
        document.setNomReel(originalFilename);
        document.setContentType(validatedPdf.contentType());
        document.setTaille((long) validatedPdf.bytes().length);
        document.setSha256(validatedPdf.sha256());
        document = documentRepository.saveAndFlush(document);

        try {
            Path documentPath = getDocumentPath(document);
            Files.createDirectories(documentPath.getParent());
            Files.write(documentPath, validatedPdf.bytes());
        } catch (IOException e) {
            logger.error("Erreur lors de l'upload du document étudiant", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'upload du fichier");
        }

        return buildResponse(convention);
    }

    @Transactional
    public ConventionDocumentsResponseDto deleteDocument(int idConvention, int idDocument) {
        Convention convention = getConventionForWrite(idConvention);
        ConventionDocumentEtudiant document = getDocument(convention, idDocument);
        deleteDocument(convention, document);
        return buildResponse(convention);
    }

    @Transactional
    public void deleteAllForConvention(Convention convention) {
        if (convention == null) {
            return;
        }
        documentRepository.findByConventionIdOrderByDateCreationDesc(convention.getId())
                .forEach(document -> deleteDocument(convention, document));
    }

    private void deleteDocument(Convention convention, ConventionDocumentEtudiant document) {
        Path filePath = getDocumentPath(document);

        ConventionDocumentEtudiantHistorique historique = new ConventionDocumentEtudiantHistorique();
        historique.setIdConvention(convention.getId());
        historique.setIdDocumentEtudiant(document.getId());
        historique.setNom(document.getNom());
        historique.setNomReel(document.getNomReel());
        historique.setContentType(document.getContentType());
        historique.setTaille(document.getTaille());
        historique.setSha256(document.getSha256());
        historique.setLoginDepot(document.getLoginCreation());
        historique.setDateDepot(document.getDateCreation());
        historiqueRepository.save(historique);

        documentRepository.delete(document);
        documentRepository.flush();

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            logger.error("Erreur lors de la suppression du document étudiant", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la suppression du fichier");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> preview(int idConvention, int idDocument) {
        return fileResponse(idConvention, idDocument, true);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> download(int idConvention, int idDocument) {
        return fileResponse(idConvention, idDocument, false);
    }

    private ResponseEntity<byte[]> fileResponse(int idConvention, int idDocument, boolean inline) {
        Convention convention = getConventionForRead(idConvention);
        ConventionDocumentEtudiant document = getDocument(convention, idDocument);
        Path filePath = getDocumentPath(document);
        if (!Files.exists(filePath)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Fichier non trouvé");
        }

        try {
            ContentDisposition contentDisposition = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                    .filename(document.getNomReel(), StandardCharsets.UTF_8)
                    .build();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .body(Files.readAllBytes(filePath));
        } catch (IOException e) {
            logger.error("Erreur lors de la lecture du document étudiant", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur sur le téléchargement du fichier");
        }
    }

    private Convention getConventionForRead(int idConvention) {
        Convention convention = conventionJpaRepository.findById(idConvention);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        conventionService.canViewEditConvention(convention, ServiceContext.getUtilisateur());
        return convention;
    }

    private Convention getConventionForWrite(int idConvention) {
        Convention convention = getConventionForRead(idConvention);
        if (!canWrite(ServiceContext.getUtilisateur())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Vous n'êtes pas autorisé à modifier les documents de cette convention");
        }
        return convention;
    }

    private boolean canWrite(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return false;
        }
        return UtilisateurHelper.isRole(utilisateur, Role.ADM)
                || UtilisateurHelper.isRole(utilisateur, Role.RESP_GES)
                || UtilisateurHelper.isRole(utilisateur, Role.GES)
                || UtilisateurHelper.isRole(utilisateur, Role.ETU);
    }

    private ConventionDocumentEtudiant getDocument(Convention convention, int idDocument) {
        ConventionDocumentEtudiant document = documentRepository.findById(idDocument);
        if (document == null || document.getConvention() == null || document.getConvention().getId() != convention.getId()) {
            throw new AppException(HttpStatus.NOT_FOUND, "Document non trouvé");
        }
        return document;
    }

    private ConventionDocumentsResponseDto buildResponse(Convention convention) {
        ConfigGeneraleDto config = appConfigService.getConfigGenerale();
        boolean canWrite = canWrite(ServiceContext.getUtilisateur());

        ConventionDocumentsResponseDto response = new ConventionDocumentsResponseDto();
        response.setMessage(config.getMessageDepotDocuments());
        response.setTailleMaxMo(getMaxSizeMo(config));
        response.setCanUpload(canWrite);
        response.setCanDelete(canWrite);
        response.setCanDownload(true);
        response.setCanPreview(true);
        response.setDocuments(documentRepository.findByConventionIdOrderByDateCreationDesc(convention.getId()).stream()
                .map(ConventionDocumentEtudiantDto::from)
                .toList());
        return response;
    }

    private int getMaxSizeMo() {
        return getMaxSizeMo(appConfigService.getConfigGenerale());
    }

    private int getMaxSizeMo(ConfigGeneraleDto config) {
        return config.getTailleMaxDepotDocumentsMo() > 0 ? config.getTailleMaxDepotDocumentsMo() : DEFAULT_MAX_SIZE_MO;
    }

    private Path getDocumentPath(ConventionDocumentEtudiant document) {
        Path root = Paths.get(appliProperties.getDataDir() + FolderEnum.CONVENTION_DOCUMENTS_ETUDIANT).normalize();
        Path path = root.resolve(document.getNom()).normalize();
        if (!path.startsWith(root)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide");
        }
        return path;
    }
}