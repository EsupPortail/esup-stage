package org.esup_portail.esup_stage.scheduler.SchedulableTasks;

import org.esup_portail.esup_stage.service.archivage.ArchivageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Archive les conventions dont le stage est terminé depuis plus de N années (5 ans par
 * défaut, configurable dans la configuration générale) ainsi que les structures dont
 * toutes les conventions sont archivées.
 */
@Component("ArchiverConventions")
public class ArchiverConventionsTask implements SchedulableTask {

    @Autowired
    private ArchivageService archivageService;

    @Override
    public Runnable getRunnable() {
        return () -> archivageService.archiver();
    }
}
