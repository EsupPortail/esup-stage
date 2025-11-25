package org.esup_portail.esup_stage.scheduler.SchedulableTasks;

import jakarta.annotation.PostConstruct;

public interface SchedulableTask {

    @PostConstruct
    void init();

    String getName();

    String getCronExpression();

    boolean isEnable();

    Runnable getRunnable();
}
