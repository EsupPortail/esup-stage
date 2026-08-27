package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.enums.FolderEnum;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.ConventionDocumentEtudiant;
import org.esup_portail.esup_stage.repository.ConventionDocumentEtudiantJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tri des fichiers des documents déposés lors de l'archivage d'une convention : les fichiers
 * sont déplacés vers le dossier d'archives du serveur (un dossier par convention).
 */
class ConventionDocumentEtudiantServiceArchivageTest {

    private ConventionDocumentEtudiantService service;
    private ConventionDocumentEtudiantJpaRepository documentRepository;
    private AppliProperties appliProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new ConventionDocumentEtudiantService();
        documentRepository = mock(ConventionDocumentEtudiantJpaRepository.class);
        appliProperties = mock(AppliProperties.class);
        ReflectionTestUtils.setField(service, "documentRepository", documentRepository);
        ReflectionTestUtils.setField(service, "appliProperties", appliProperties);
        when(appliProperties.getDataDir()).thenReturn(tempDir.toString());
    }

    private Path racineDocuments() {
        return Paths.get(tempDir.toString() + FolderEnum.CONVENTION_DOCUMENTS_ETUDIANT).normalize();
    }

    @Test
    void archiverFichiersDeplaceLesDocumentsVersLeDossierArchive() throws Exception {
        Convention convention = new Convention();
        convention.setId(42);
        ConventionDocumentEtudiant document = new ConventionDocumentEtudiant();
        document.setNom("doc-archive.pdf");
        ConventionDocumentEtudiant documentSansFichier = new ConventionDocumentEtudiant();
        documentSansFichier.setNom("doc-absent.pdf");
        when(documentRepository.findByConventionIdOrderByDateCreationDesc(42)).thenReturn(List.of(document, documentSansFichier));
        Files.createDirectories(racineDocuments());
        Files.write(racineDocuments().resolve("doc-archive.pdf"), "PDF".getBytes(StandardCharsets.UTF_8));

        Path dossierArchive = tempDir.resolve("archives").resolve("convention_42");
        int nb = service.archiverFichiers(convention, dossierArchive);

        // Seul le fichier présent est déplacé, le document au fichier déjà absent est ignoré sans erreur
        assertThat(nb).isEqualTo(1);
        assertThat(racineDocuments().resolve("doc-archive.pdf")).doesNotExist();
        assertThat(dossierArchive.resolve("doc-archive.pdf")).exists();

        assertThat(service.archiverFichiers(null, dossierArchive)).isZero(); // ne fait rien
    }
}
