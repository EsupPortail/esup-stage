package org.esup_portail.esup_stage.controller.admin;

import lombok.Data;
import org.esup_portail.esup_stage.controller.ApiController;
import org.esup_portail.esup_stage.dto.FileContentDto;
import org.esup_portail.esup_stage.dto.FileElementDto;
import org.esup_portail.esup_stage.dto.LoggerLevelDto;
import org.esup_portail.esup_stage.dto.LoggerUpdateRequest;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.logs.ExportService;
import org.esup_portail.esup_stage.service.logs.LogFileService;
import org.esup_portail.esup_stage.service.logs.LogReaderService;
import org.esup_portail.esup_stage.service.logs.LogTailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@ApiController
@RequestMapping("/admin/logs")
public class AdminLogsController {

    private static final int INITIAL_HISTORY_SIZE = 200;

    @Autowired
    private LogTailerService logTailerService;

    @Autowired
    private LoggingSystem loggingSystem;

    @Autowired
    private LogFileService logFileService;

    @Autowired
    private LogReaderService logReaderService;

    @Autowired
    private ExportService exportService;


    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Secure
    public SseEmitter stream() {
        requireAdmin();

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        AtomicReference<UUID> streamId = new AtomicReference<>();
        Runnable cleanup = () -> {
            UUID id = streamId.get();
            if (id != null) {
                logTailerService.stopStreaming(id);
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());

        streamId.set(logTailerService.startStreaming(emitter, INITIAL_HISTORY_SIZE));
        return emitter;
    }

    @GetMapping()
    public ResponseEntity<List<LoggerLevelDto>> getLoggers() {
        requireAdmin();
        List<LoggerLevelDto> loggers = loggingSystem.getLoggerConfigurations()
                .stream()
                // .filter(config -> config.getName() != null &&
                // config.getName().contains("fr.univlorraine"))
                .map(config -> new LoggerLevelDto(
                        config.getName(),
                        config.getConfiguredLevel() != null ? config.getConfiguredLevel().name() : null,
                        config.getEffectiveLevel() != null ? config.getEffectiveLevel().name() : null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(loggers);
    }

    @PostMapping()
    public ResponseEntity<Void> updateLoggers(@RequestBody LoggerUpdateRequest request) {
        requireAdmin();
        if (request.getPackageNames() == null || request.getLevel() == null) {
            return ResponseEntity.badRequest().build();
        }

        LogLevel logLevel;
        try {
            logLevel = LogLevel.valueOf(request.getLevel().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        for (String packageName : request.getPackageNames()) {
            loggingSystem.setLogLevel(packageName, logLevel);
        }

        return ResponseEntity.noContent().build();
    }

    /** Liste les fichiers/dossiers d'un chemin */
    @GetMapping("/list")
    public ResponseEntity<List<FileElementDto>> listFolder(@RequestParam(name = "path", defaultValue = "") String path) throws IOException {
        requireAdmin();
        return ResponseEntity.ok(logFileService.listFolder(path));
    }

    /** Contenu paginÃ© d'un fichier log */
    @GetMapping("/content")
    public ResponseEntity<FileContentDto> getContent(
            @RequestParam(name = "path") String path,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "500") int pageSize
    ) throws IOException {
        requireAdmin();
        return ResponseEntity.ok(logReaderService.readPage(path, page, pageSize));
    }

    /** CrÃ©e un sous-dossier */
    @PostMapping("/folder")
    public ResponseEntity<FileElementDto> createFolder(@RequestBody CreateFolderRequest req) throws IOException {
        return ResponseEntity.ok(logFileService.createFolder(req.getParentPath(), req.getName()));
    }

    /** DÃ©place un fichier/dossier */
    @PutMapping("/move")
    public ResponseEntity<FileElementDto> moveElement(@RequestBody MoveRequest req) throws IOException {
        return ResponseEntity.ok(logFileService.moveElement(req.getSourcePath(), req.getTargetFolderPath()));
    }

    /** Renomme un fichier/dossier */
    @PutMapping("/rename")
    public ResponseEntity<FileElementDto> renameElement(@RequestBody RenameRequest req) throws IOException {
        return ResponseEntity.ok(logFileService.renameElement(req.getPath(), req.getNewName()));
    }

    /** Supprime un fichier/dossier */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteElement(@RequestParam(name = "path") String path) throws IOException {
        logFileService.deleteElement(path);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exporte un fichier unique en tÃ©lÃ©chargement direct (.log, .txt, etc.)
     * GET /api/files/export/single?path=/logs/app.log
     */
    @GetMapping("/export/single")
    public ResponseEntity<Resource> exportSingle(@RequestParam(name = "path") String path) throws IOException {
        Resource resource = exportService.exportSingle(path);
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Exporte un fichier log au format CSV (timestamp, level, thread, message).
     * GET /api/files/export/csv?path=/logs/app.log
     */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportAsCsv(@RequestParam(name = "path") String path) throws IOException {
        byte[] csv = exportService.exportLogAsCsv(path);
        String fileName = path.substring(path.lastIndexOf('/') + 1).replaceAll("\\.\\w+$", "") + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    /** TÃ©lÃ©chargement interne (bouton "TÃ©lÃ©charger" de l'explorateur) */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam(name = "path") String path) throws IOException {
        Resource resource = logFileService.getResource(path);
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


    private Utilisateur requireAdmin() {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (utilisateur == null || !UtilisateurHelper.isAdmin(utilisateur)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Acces interdit");
        }
        return utilisateur;
    }

    @Data
    public static class CreateFolderRequest { String parentPath; String name; }
    @Data
    public static class MoveRequest         { String sourcePath; String targetFolderPath; }
    @Data
    public static class RenameRequest       { String path; String newName; }

}
