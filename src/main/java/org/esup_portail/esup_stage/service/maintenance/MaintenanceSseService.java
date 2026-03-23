package org.esup_portail.esup_stage.service.maintenance;

import org.esup_portail.esup_stage.dto.MaintenanceStateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MaintenanceSseService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceSseService.class);

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
    }

    public void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    public void broadcast(MaintenanceStateDto state) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("maintenance").data(state));
            } catch (Exception e) {
                if (isClientDisconnectException(e)) {
                    log.debug("Maintenance SSE client disconnected: {}", e.getMessage());
                } else {
                    log.warn("Maintenance SSE broadcast failed: {}", e.getMessage());
                }
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
        for (SseEmitter deadEmitter : deadEmitters) {
            try {
                deadEmitter.complete();
            } catch (Exception ignored) {
                // Client is already disconnected/completed.
            }
        }
    }

    private boolean isClientDisconnectException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase(Locale.ROOT);
                if (normalized.contains("broken pipe")
                        || normalized.contains("connection reset by peer")
                        || normalized.contains("forcibly closed by the remote host")
                        || normalized.contains("une connexion etablie a ete abandonnee")
                        || normalized.contains("une connexion établie a été abandonnée")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
