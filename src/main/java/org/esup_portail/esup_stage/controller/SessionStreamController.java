package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.service.session.SessionSseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ApiController
@RequestMapping("/sessions")
public class SessionStreamController {

    @Autowired
    private SessionSseService sessionSseService;

    /**
     * Flux SSE ouvert par tous les utilisateurs connectés : permet de recevoir l'événement
     * "force-logout" lorsque leur session est fermée par un administrateur.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Session non authentifiée");
        }
        String sessionId = session.getId();

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        Runnable cleanup = () -> sessionSseService.removeEmitter(sessionId, emitter);

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());

        sessionSseService.addEmitter(sessionId, emitter);

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (Exception e) {
            cleanup.run();
            emitter.completeWithError(e);
        }

        return emitter;
    }
}
