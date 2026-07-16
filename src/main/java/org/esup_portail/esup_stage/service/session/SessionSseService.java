package org.esup_portail.esup_stage.service.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Gère les connexions SSE ouvertes par les clients, indexées par identifiant de session HTTP,
 * afin de pouvoir notifier immédiatement un client dont la session est fermée par un administrateur.
 */
@Service
public class SessionSseService {

    private static final Logger log = LoggerFactory.getLogger(SessionSseService.class);

    public static final String FORCE_LOGOUT_EVENT = "force-logout";

    private final Map<String, List<SseEmitter>> emittersBySessionId = new ConcurrentHashMap<>();

    public void addEmitter(String sessionId, SseEmitter emitter) {
        emittersBySessionId.computeIfAbsent(sessionId, key -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public void removeEmitter(String sessionId, SseEmitter emitter) {
        emittersBySessionId.computeIfPresent(sessionId, (key, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }

    public void sendForceLogout(String sessionId) {
        List<SseEmitter> emitters = emittersBySessionId.remove(sessionId);
        if (emitters == null) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(FORCE_LOGOUT_EVENT).data("admin"));
                emitter.complete();
            } catch (Exception e) {
                log.debug("Envoi force-logout impossible (client déjà déconnecté ?) : {}", e.getMessage());
                try {
                    emitter.complete();
                } catch (Exception ignored) {
                    // Client déjà déconnecté.
                }
            }
        }
    }
}
