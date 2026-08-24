package org.esup_portail.esup_stage.scheduler.SchedulableTasks;

import org.esup_portail.esup_stage.service.archivage.ArchivageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Supprime définitivement les conventions archivées depuis plus de M années (2 ans par
 * défaut, configurable dans la configuration générale) ainsi que les structures archivées
 * qui ne sont plus référencées. Tâche inactive par défaut : la purge est irréversible,
 * chaque établissement doit l'activer explicitement.
 */
@Component("PurgerConventions")
public class PurgerConventionsTask implements SchedulableTask {

    @Autowired
    private ArchivageService archivageService;

    @Override
    public Runnable getRunnable() {
        return () -> archivageService.purger();
    }
}
