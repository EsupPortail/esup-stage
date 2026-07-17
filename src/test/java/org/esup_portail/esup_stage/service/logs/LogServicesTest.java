package org.esup_portail.esup_stage.service.logs;

import org.esup_portail.esup_stage.dto.FileContentDto;
import org.esup_portail.esup_stage.dto.FileElementDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests des services de gestion des fichiers de logs (lecture paginée,
 * navigation, opérations sur les fichiers) sur un répertoire temporaire.
 */
class LogServicesTest {

    @TempDir
    Path racine;

    private LogReaderService reader;
    private LogFileService files;

    @BeforeEach
    void setUp() {
        reader = new LogReaderService();
        files = new LogFileService();
        ReflectionTestUtils.setField(reader, "rootPath", racine.toString());
        ReflectionTestUtils.setField(files, "rootPath", racine.toString());
    }

    private Path fichierDeLignes(String nom, int nbLignes) throws IOException {
        String contenu = IntStream.rangeClosed(1, nbLignes)
                .mapToObj(i -> "ligne-" + i)
                .collect(Collectors.joining("\n"));
        Path fichier = racine.resolve(nom);
        Files.writeString(fichier, contenu, StandardCharsets.UTF_8);
        return fichier;
    }

    // ------------------------------------------------------------------
    // LogReaderService
    // ------------------------------------------------------------------

    @Test
    void lectureDeLaPremierePage() throws IOException {
        fichierDeLignes("app.log", 25);

        FileContentDto page = reader.readPage("app.log", 0, 10);

        assertThat(page.getFileName()).isEqualTo("app.log");
        assertThat(page.getTotalLines()).isEqualTo(25);
        assertThat(page.getPage()).isZero();
        assertThat(page.getPageSize()).isEqualTo(10);
        assertThat(page.getContent()).startsWith("ligne-1\n").endsWith("ligne-10");
    }

    @Test
    void lectureDeLaDernierePagePartielle() throws IOException {
        fichierDeLignes("app.log", 25);

        FileContentDto page = reader.readPage("app.log", 2, 10);

        assertThat(page.getContent()).startsWith("ligne-21").endsWith("ligne-25");
    }

    @Test
    void lesParametresAberrantsSontBornes() throws IOException {
        fichierDeLignes("app.log", 5);

        FileContentDto page = reader.readPage("app.log", -3, 0);

        assertThat(page.getPage()).isZero();
        assertThat(page.getPageSize()).isEqualTo(1);
        assertThat(page.getContent()).isEqualTo("ligne-1");
    }

    @Test
    void laTraverseeDeCheminEstBloquee() throws IOException {
        fichierDeLignes("app.log", 1);

        assertThatThrownBy(() -> reader.readPage("../secret.txt", 0, 10))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void unDossierNEstPasLisibleCommeFichier() throws IOException {
        Files.createDirectory(racine.resolve("dossier"));

        assertThatThrownBy(() -> reader.readPage("dossier", 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------
    // LogFileService
    // ------------------------------------------------------------------

    @Test
    void listeLaRacineDossiersDAbord() throws IOException {
        Files.createDirectory(racine.resolve("zdossier"));
        fichierDeLignes("app.log", 1);

        List<FileElementDto> elements = files.listFolder("/");

        assertThat(elements).hasSize(2);
        assertThat(elements.get(0).getName()).isEqualTo("zdossier");
        assertThat(elements.get(0).isFolder()).isTrue();
        assertThat(elements.get(1).getName()).isEqualTo("app.log");
        assertThat(elements.get(1).isFolder()).isFalse();
        assertThat(elements.get(1).getExtension()).isEqualTo("log");
        assertThat(elements.get(1).getSize()).isPositive();
    }

    @Test
    void listeUnSousDossier() throws IOException {
        Path sous = Files.createDirectory(racine.resolve("archives"));
        Files.writeString(sous.resolve("vieux.log"), "x");

        List<FileElementDto> elements = files.listFolder("archives");

        assertThat(elements).hasSize(1);
        assertThat(elements.get(0).getPath()).isEqualTo("/archives/vieux.log");
    }

    @Test
    void listerUnCheminInexistantEchoue() {
        assertThatThrownBy(() -> files.listFolder("inconnu"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> files.listFolder("../ailleurs"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void creeDeplaceRenommeEtSupprime() throws IOException {
        FileElementDto dossier = files.createFolder("/", "archives");
        assertThat(dossier.isFolder()).isTrue();
        assertThat(Files.isDirectory(racine.resolve("archives"))).isTrue();

        fichierDeLignes("app.log", 1);
        FileElementDto deplace = files.moveElement("app.log", "archives");
        assertThat(deplace.getName()).isEqualTo("app.log");
        assertThat(Files.exists(racine.resolve("archives/app.log"))).isTrue();

        FileElementDto renomme = files.renameElement("/archives/app.log", "ancien.log");
        assertThat(renomme.getName()).isEqualTo("ancien.log");
        assertThat(Files.exists(racine.resolve("archives/ancien.log"))).isTrue();

        // suppression récursive du dossier
        files.deleteElement("/archives");
        assertThat(Files.exists(racine.resolve("archives"))).isFalse();
    }

    @Test
    void supprimeUnFichierSimple() throws IOException {
        fichierDeLignes("app.log", 1);

        files.deleteElement("app.log");

        assertThat(Files.exists(racine.resolve("app.log"))).isFalse();
    }

    @Test
    void fournitUneRessourceTelechargeable() throws IOException {
        fichierDeLignes("app.log", 2);

        assertThat(files.getResource("app.log").exists()).isTrue();
        assertThatThrownBy(() -> files.getResource("absent.log"))
                .isInstanceOf(NoSuchFileException.class);
    }
}
