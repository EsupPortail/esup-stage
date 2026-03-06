package org.esup_portail.esup_stage.service.maintenance;

import org.esup_portail.esup_stage.dto.MaintenanceStateDto;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MaintenanceSseService {

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
                deadEmitters.add(emitter);
            }
        }

        for (SseEmitter deadEmitter : deadEmitters) {
            emitters.remove(deadEmitter);
            deadEmitter.complete();
        }
    }
}
