package org.esup_portail.esup_stage.controller.admin;

import org.esup_portail.esup_stage.controller.ApiController;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.LogTailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@ApiController
@RequestMapping("/admin/logs")
public class AdminLogsController {

    private static final int INITIAL_HISTORY_SIZE = 200;

    @Autowired
    private LogTailerService logTailerService;

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

    private Utilisateur requireAdmin() {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (utilisateur == null || !UtilisateurHelper.isAdmin(utilisateur)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Acces interdit");
        }
        return utilisateur;
    }
}
