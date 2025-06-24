package org.esup_portail.esup_stage.service.siren.utils;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Objects;

@Slf4j
public class ApiSireneWorker implements Runnable {
    private final RateLimiter rateLimiter = RateLimiter.create(0.5); // 30/min

    @Override
    public void run() {
        while (true) {
            try {
                Runnable task = TaskQueue.take();
                log.info("➡ [TaskQueue] Requète vers l'api sirene reçu à {}", LocalTime.now());
                rateLimiter.acquire(); // attend ici si trop rapide
                log.info("⏳ [RateLimiter] Requète envoyé à {}", LocalTime.now());
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
