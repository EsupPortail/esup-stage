package org.esup_portail.esup_stage.controller.admin;

import org.esup_portail.esup_stage.controller.ApiController;
import org.esup_portail.esup_stage.dto.FileContentDto;
import org.esup_portail.esup_stage.dto.FileElementDto;
import org.esup_portail.esup_stage.dto.LoggerLevelDto;
import org.esup_portail.esup_stage.dto.LoggerUpdateRequest;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AdminService;
import org.esup_portail.esup_stage.service.logs.ExportService;
import org.esup_portail.esup_stage.service.logs.LogFileService;
import org.esup_portail.esup_stage.service.logs.LogReaderService;
import org.esup_portail.esup_stage.service.logs.LogTailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
    
    @Autowired
    private AdminService adminService;


    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public SseEmitter stream() {
        adminService.requireAdmin();

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
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<List<LoggerLevelDto>> getLoggers() {
        adminService.requireAdmin();
        List<LoggerLevelDto> loggers = loggingSystem.getLoggerConfigurations()
                .stream()
                // .filter(config -> config.getName() != null &&
                // config.getName().contains("fr.univlorraine"))
                .map(config -> new LoggerLevelDto(
                        config.getName(),
                        config.getConfiguredLevel() != null ? config.getConfiguredLevel().name() : null,
                        config.getEffectiveLevel() != null ? config.getEffectiveLevel().name() : null))
                .toList();

        return ResponseEntity.ok(loggers);
    }

    @PostMapping()
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public ResponseEntity<Void> updateLoggers(@RequestBody LoggerUpdateRequest request) {
        adminService.requireAdmin();
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
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<List<FileElementDto>> listFolder(@RequestParam(name = "path", defaultValue = "") String path) throws IOException {
        adminService.requireAdmin();
        return ResponseEntity.ok(logFileService.listFolder(path));
    }

    /** Contenu paginé d'un fichier log */
    @GetMapping("/content")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<FileContentDto> getContent(
            @RequestParam(name = "path") String path,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "500") int pageSize
    ) throws IOException {
        adminService.requireAdmin();
        return ResponseEntity.ok(logReaderService.readPage(path, page, pageSize));
    }

    /**
     * Exporte un fichier unique en téléchargement direct (.log, .txt, etc.)
     * GET /api/files/export/single?path=/logs/app.log
     */
    @GetMapping("/export/single")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<Resource> exportSingle(@RequestParam(name = "path") String path) throws IOException {
        adminService.requireAdmin();
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
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportAsCsv(@RequestParam(name = "path") String path) throws IOException {
        adminService.requireAdmin();
        byte[] csv = exportService.exportLogAsCsv(path);
        String fileName = path.substring(path.lastIndexOf('/') + 1).replaceAll("\\.\\w+$", "") + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    /** Téléchargement interne (bouton "Télécharger" de l'explorateur) */
    @GetMapping("/download")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<Resource> downloadFile(@RequestParam(name = "path") String path) throws IOException {
        adminService.requireAdmin();
        Resource resource = logFileService.getResource(path);
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
    
}
