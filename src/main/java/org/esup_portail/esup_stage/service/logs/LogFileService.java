package org.esup_portail.esup_stage.service.logs;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.FileElementDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LogFileService {

    @Value("${appli.logs_dir}")
    private String rootPath;

    /** Sécurisation : s'assure que le chemin reste sous rootPath */
    private Path securePath(String relativePath) {
        Path root = Paths.get(rootPath).toAbsolutePath().normalize();
        Path resolved = root.resolve(relativePath.startsWith("/")
                        ? relativePath.substring(1) : relativePath)
                .toAbsolutePath().normalize();

        if (!resolved.startsWith(root)) {
            throw new SecurityException("Accès refusé : chemin hors de la racine");
        }
        return resolved;
    }

    public List<FileElementDto> listFolder(String path) throws IOException {
        Path folder;
        String normalizedPath;

        // Si le chemin est vide ou "/", utiliser directement rootPath
        if (path == null || path.isEmpty() || path.equals("/")) {
            folder = Paths.get(rootPath).toAbsolutePath().normalize();
            normalizedPath = "/";
        } else {
            folder = securePath(path);
            normalizedPath = path.startsWith("/") ? path : "/" + path;
        }

        // Vérifier que c'est un dossier
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Le chemin n'est pas un dossier : " + path);
        }

        try (var stream = Files.list(folder)) {
            return stream
                    .map(p -> toDto(p, normalizedPath))
                    .sorted((a, b) -> {
                        if (a.isFolder() != b.isFolder()) return a.isFolder() ? -1 : 1;
                        return a.getName().compareToIgnoreCase(b.getName());
                    })
                    .collect(Collectors.toList());
        }
    }

    public FileElementDto createFolder(String parentPath, String name) throws IOException {
        Path parent = securePath(parentPath);
        Path newFolder = parent.resolve(name);
        Files.createDirectory(newFolder);
        return toDto(newFolder, parentPath + "/" + name);
    }

    public FileElementDto moveElement(String sourcePath, String targetFolderPath) throws IOException {
        Path source = securePath(sourcePath);
        Path targetFolder = securePath(targetFolderPath);
        Path target = targetFolder.resolve(source.getFileName());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        return toDto(target, targetFolderPath + "/" + source.getFileName().toString());
    }

    public FileElementDto renameElement(String path, String newName) throws IOException {
        Path source = securePath(path);
        Path renamed = source.resolveSibling(newName);
        Files.move(source, renamed);
        String parentPath = path.substring(0, path.lastIndexOf('/'));
        return toDto(renamed, parentPath + "/" + newName);
    }

    public void deleteElement(String path) throws IOException {
        Path target = securePath(path);
        if (Files.isDirectory(target)) {
            deleteRecursively(target);
        } else {
            Files.deleteIfExists(target);
        }
    }

    public Resource getResource(String path) throws IOException {
        Path target = securePath(path);
        if (!Files.exists(target)) throw new NoSuchFileException(path);
        return new FileSystemResource(target);
    }

    // ── Helpers ──────────────────────────────────────────────────

    private FileElementDto toDto(Path path, String parentPath) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            boolean isFolder = Files.isDirectory(path);
            String name = path.getFileName().toString();
            String ext = isFolder ? null : getExtension(name);

            // Construire le chemin virtuel de cet élément
            String elementPath = parentPath.equals("/") ? "/" + name : parentPath + "/" + name;

            // Construire le parent path en sécurité
            String parentPathForElement;
            if (parentPath.equals("/")) {
                parentPathForElement = "/";
            } else {
                int lastSlash = parentPath.lastIndexOf('/');
                parentPathForElement = lastSlash > 0 ? parentPath.substring(0, lastSlash) : "/";
            }

            return FileElementDto.builder()
                    .id(UUID.nameUUIDFromBytes(elementPath.getBytes()).toString())
                    .name(name)
                    .path(elementPath)
                    .isFolder(isFolder)
                    .size(isFolder ? null : attrs.size())
                    .lastModified(Date.from(attrs.lastModifiedTime().toInstant()))
                    .extension(ext)
                    .parent(parentPathForElement)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture attributs : " + path, e);
        }
    }

    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot + 1).toLowerCase() : "";
    }

    private void deleteRecursively(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException {
                Files.delete(f); return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
                Files.delete(d); return FileVisitResult.CONTINUE;
            }
        });
    }
}
