package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.MaintenanceStateDto;
import org.esup_portail.esup_stage.service.maintenance.MaintenanceService;
import org.esup_portail.esup_stage.service.maintenance.MaintenanceSseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ApiController
@RequestMapping("/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private MaintenanceSseService maintenanceSseService;

    @GetMapping("/status")
    public MaintenanceStateDto status() {
        return maintenanceService.getCurrentState();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        Runnable cleanup = () -> maintenanceSseService.removeEmitter(emitter);

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());

        maintenanceSseService.addEmitter(emitter);

        try {
            MaintenanceStateDto currentState = maintenanceService.getCurrentState();
            emitter.send(SseEmitter.event().name("maintenance").data(currentState));
        } catch (Exception e) {
            cleanup.run();
            emitter.completeWithError(e);
        }

        return emitter;
    }
}
