package org.esup_portail.esup_stage.service.logs;


import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.FileContentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogReaderService {

    @Value("${appli.logs_dir}")
    private String rootPath;

    public FileContentDto readPage(String path, int page, int pageSize) throws IOException {
        Path filePath = resolveAndValidate(path);

        // Comptage rapide du nombre total de lignes
        long totalLines = countLines(filePath);

        // Lecture des lignes de la page demandée
        long startLine = (long) page * pageSize;
        String content = readLines(filePath, startLine, pageSize);

        return FileContentDto.builder()
                .fileName(filePath.getFileName().toString())
                .content(content)
                .totalLines(totalLines)
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    private long countLines(Path file) throws IOException {
        long count = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            while (reader.readLine() != null) count++;
        } catch (Exception e) {
            // Fallback pour les encodages non-UTF8
            try (InputStream is = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    for (int i = 0; i < read; i++) {
                        if (buffer[i] == '\n') count++;
                    }
                }
                count++; // Dernière ligne sans \n
            }
        }
        return count;
    }

    private String readLines(Path file, long startLine, int pageSize) throws IOException {
        List<String> lines = new ArrayList<>(pageSize);
        long currentLine = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file), detectCharset(file)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (currentLine >= startLine && lines.size() < pageSize) {
                    lines.add(line);
                }
                currentLine++;
                if (lines.size() >= pageSize) break;
            }
        }

        return String.join("\n", lines);
    }

    private String detectCharset(Path file) {
        // Détection simple : UTF-8 par défaut, fallback ISO-8859-1
        try (InputStream is = Files.newInputStream(file)) {
            byte[] bom = new byte[3];
            if (is.read(bom) >= 3 && bom[0] == (byte)0xEF && bom[1] == (byte)0xBB && bom[2] == (byte)0xBF) {
                return "UTF-8";
            }
        } catch (IOException ignored) {}
        return "UTF-8"; // Tenter UTF-8, l'InputStreamReader ignorera les erreurs
    }

    private Path resolveAndValidate(String relativePath) {
        Path root = Paths.get(rootPath).toAbsolutePath().normalize();
        String cleaned = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        Path resolved = root.resolve(cleaned).toAbsolutePath().normalize();

        if (!resolved.startsWith(root)) {
            throw new SecurityException("Chemin invalide : hors de la racine");
        }
        if (!Files.isRegularFile(resolved)) {
            throw new IllegalArgumentException("Ce n'est pas un fichier : " + relativePath);
        }
        return resolved;
    }
}
