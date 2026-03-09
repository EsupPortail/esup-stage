package org.esup_portail.esup_stage.controller.admin;

import org.esup_portail.esup_stage.controller.ApiController;
import org.esup_portail.esup_stage.dto.LoggerLevelDto;
import org.esup_portail.esup_stage.dto.LoggerUpdateRequest;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.LogTailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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


    private Utilisateur requireAdmin() {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (utilisateur == null || !UtilisateurHelper.isAdmin(utilisateur)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Acces interdit");
        }
        return utilisateur;
    }
}
