package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.ConventionDocumentsResponseDto;
import org.esup_portail.esup_stage.enums.FolderEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.ConventionDocumentEtudiant;
import org.esup_portail.esup_stage.model.ConventionDocumentEtudiantHistorique;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.ConventionDocumentEtudiantHistoriqueJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionDocumentEtudiantJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConventionDocumentEtudiantServiceTest {

    private ConventionDocumentEtudiantService service;
    private ConventionJpaRepository conventionJpaRepository;
    private ConventionDocumentEtudiantJpaRepository documentRepository;
    private ConventionDocumentEtudiantHistoriqueJpaRepository historiqueRepository;
    private ConventionService conventionService;
    private AppConfigService appConfigService;
    private AppliProperties appliProperties;
    private FileValidationService fileValidationService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new ConventionDocumentEtudiantService();
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        documentRepository = mock(ConventionDocumentEtudiantJpaRepository.class);
        historiqueRepository = mock(ConventionDocumentEtudiantHistoriqueJpaRepository.class);
        conventionService = mock(ConventionService.class);
        appConfigService = mock(AppConfigService.class);
        appliProperties = mock(AppliProperties.class);
        fileValidationService = mock(FileValidationService.class);
        ReflectionTestUtils.setField(service, "conventionJpaRepository", conventionJpaRepository);
        ReflectionTestUtils.setField(service, "documentRepository", documentRepository);
        ReflectionTestUtils.setField(service, "historiqueRepository", historiqueRepository);
        ReflectionTestUtils.setField(service, "conventionService", conventionService);
        ReflectionTestUtils.setField(service, "appConfigService", appConfigService);
        ReflectionTestUtils.setField(service, "appliProperties", appliProperties);
        ReflectionTestUtils.setField(service, "fileValidationService", fileValidationService);
        ReflectionTestUtils.setField(service, "filenameSanitizerService", new FilenameSanitizerService());

        when(appliProperties.getDataDir()).thenReturn(tempDir.toString());
        when(documentRepository.saveAndFlush(any(ConventionDocumentEtudiant.class))).thenAnswer(inv -> inv.getArgument(0));

        ConfigGeneraleDto config = mock(ConfigGeneraleDto.class);
        when(config.getMessageDepotDocuments()).thenReturn("Déposez vos documents");
        when(config.getTailleMaxDepotDocumentsMo()).thenReturn(5);
        when(appConfigService.getConfigGenerale()).thenReturn(config);

        Convention convention = new Convention();
        convention.setId(42);
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        when(documentRepository.findByConventionIdOrderByDateCreationDesc(42)).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void connecte(String uid, String... roleCodes) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid(uid);
        utilisateur.setLogin(uid);
        utilisateur.setRoles(java.util.Arrays.stream(roleCodes).map(code -> {
            Role role = new Role();
            role.setCode(code);
            return role;
        }).toList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CasUserDetailsImpl(utilisateur, List.of()), null));
    }

    private Path racineDocuments() {
        return Paths.get(tempDir.toString() + FolderEnum.CONVENTION_DOCUMENTS_ETUDIANT).normalize();
    }

    private FileValidationService.ValidatedPdf pdfValide() {
        return new FileValidationService.ValidatedPdf("PDF".getBytes(StandardCharsets.UTF_8), "application/pdf", "pdf", "hash");
    }

    @Test
    void listRetourneLesDroitsEtLaConfiguration() {
        connecte("ges1", Role.GES);

        ConventionDocumentsResponseDto reponse = service.list(42);

        assertThat(reponse.getMessage()).isEqualTo("Déposez vos documents");
        assertThat(reponse.getTailleMaxMo()).isEqualTo(5);
        assertThat(reponse.isCanUpload()).isTrue();
        assertThat(reponse.isCanDownload()).isTrue();
        assertThat(reponse.getDocuments()).isEmpty();

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> service.list(99)).isInstanceOf(AppException.class);
    }

    @Test
    void laTailleMaximaleInvalideRetombeSurLeDefaut() {
        connecte("etu1", Role.ETU);
        ConfigGeneraleDto configInvalide = mock(ConfigGeneraleDto.class);
        when(configInvalide.getTailleMaxDepotDocumentsMo()).thenReturn(0);
        when(appConfigService.getConfigGenerale()).thenReturn(configInvalide);

        assertThat(service.list(42).getTailleMaxMo()).isEqualTo(10);

        when(configInvalide.getTailleMaxDepotDocumentsMo()).thenReturn(500);
        assertThat(service.list(42).getTailleMaxMo()).isEqualTo(10);
    }

    @Test
    void addDocumentEcritLeFichierSurDisque() throws Exception {
        connecte("etu1", Role.ETU);
        when(fileValidationService.validatePdf(any(), anyInt())).thenReturn(pdfValide());
        when(documentRepository.findByConventionIdAndNomReelOrderByDateCreationDesc(anyInt(), anyString())).thenReturn(List.of());

        service.addDocument(42, new MockMultipartFile("doc", "rapport.pdf", "application/pdf", "PDF".getBytes()), false);

        ArgumentCaptor<ConventionDocumentEtudiant> document = ArgumentCaptor.forClass(ConventionDocumentEtudiant.class);
        verify(documentRepository).saveAndFlush(document.capture());
        assertThat(document.getValue().getNomReel()).isEqualTo("rapport.pdf");
        assertThat(document.getValue().getSha256()).isEqualTo("hash");
        Path fichier = racineDocuments().resolve(document.getValue().getNom());
        assertThat(Files.readAllBytes(fichier)).isEqualTo("PDF".getBytes());
    }

    @Test
    void addDocumentSurUnNomExistantExigeLaConfirmation() {
        connecte("etu1", Role.ETU);
        when(fileValidationService.validatePdf(any(), anyInt())).thenReturn(pdfValide());
        ConventionDocumentEtudiant existant = new ConventionDocumentEtudiant();
        existant.setNom("ancien.pdf");
        existant.setNomReel("rapport.pdf");
        when(documentRepository.findByConventionIdAndNomReelOrderByDateCreationDesc(anyInt(), anyString()))
                .thenReturn(List.of(existant));

        MockMultipartFile doc = new MockMultipartFile("doc", "rapport.pdf", "application/pdf", "PDF".getBytes());
        assertThatThrownBy(() -> service.addDocument(42, doc, false))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.CONFLICT));

        service.addDocument(42, doc, true);
        ArgumentCaptor<ConventionDocumentEtudiantHistorique> historique =
                ArgumentCaptor.forClass(ConventionDocumentEtudiantHistorique.class);
        verify(historiqueRepository).save(historique.capture());
        assertThat(historique.getValue().getTypeAction()).isEqualTo("REMPLACEMENT");
    }

    @Test
    void unEnseignantNePeutPasDeposerDeDocument() {
        connecte("ens1", Role.ENS);
        MockMultipartFile doc = new MockMultipartFile("doc", "rapport.pdf", "application/pdf", "PDF".getBytes());

        assertThatThrownBy(() -> service.addDocument(42, doc, false))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    private ConventionDocumentEtudiant documentEnBase(String nom) {
        Convention convention = new Convention();
        convention.setId(42);
        ConventionDocumentEtudiant document = new ConventionDocumentEtudiant();
        document.setConvention(convention);
        document.setNom(nom);
        document.setNomReel("rapport.pdf");
        when(documentRepository.findById(4)).thenReturn(document);
        return document;
    }

    @Test
    void deleteDocumentSupprimeLeFichierEtTraceLHistorique() throws Exception {
        connecte("ges1", Role.GES);
        ConventionDocumentEtudiant document = documentEnBase("doc-a-supprimer.pdf");
        Files.createDirectories(racineDocuments());
        Path fichier = racineDocuments().resolve("doc-a-supprimer.pdf");
        Files.write(fichier, "PDF".getBytes());

        service.deleteDocument(42, 4);

        assertThat(Files.exists(fichier)).isFalse();
        verify(documentRepository).delete(document);
        ArgumentCaptor<ConventionDocumentEtudiantHistorique> historique =
                ArgumentCaptor.forClass(ConventionDocumentEtudiantHistorique.class);
        verify(historiqueRepository).save(historique.capture());
        assertThat(historique.getValue().getTypeAction()).isEqualTo("SUPPRESSION");

        when(documentRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> service.deleteDocument(42, 99)).isInstanceOf(AppException.class);
    }

    @Test
    void deleteAllSupprimeTousLesDocumentsDeLaConvention() {
        connecte("ges1", Role.GES);
        Convention convention = new Convention();
        convention.setId(42);
        ConventionDocumentEtudiant document = new ConventionDocumentEtudiant();
        document.setConvention(convention);
        document.setNom("doc.pdf");
        when(documentRepository.findByConventionIdOrderByDateCreationDesc(42)).thenReturn(List.of(document));

        service.deleteAllForConvention(convention);
        verify(documentRepository).delete(document);

        service.deleteAllForConvention(null); // ne fait rien
    }

    @Test
    void previewEtDownloadServentLeFichierAvecLesBonsEntetes() throws Exception {
        connecte("ges1", Role.GES);
        documentEnBase("doc-lecture.pdf");
        Files.createDirectories(racineDocuments());
        Files.write(racineDocuments().resolve("doc-lecture.pdf"), "PDF".getBytes());

        ResponseEntity<byte[]> apercu = service.preview(42, 4);
        assertThat(apercu.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).startsWith("inline");
        assertThat(apercu.getBody()).isEqualTo("PDF".getBytes());

        ResponseEntity<byte[]> telechargement = service.download(42, 4);
        assertThat(telechargement.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).startsWith("attachment");

        Files.delete(racineDocuments().resolve("doc-lecture.pdf"));
        assertThatThrownBy(() -> service.preview(42, 4))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Fichier non trouvé");
    }
}
