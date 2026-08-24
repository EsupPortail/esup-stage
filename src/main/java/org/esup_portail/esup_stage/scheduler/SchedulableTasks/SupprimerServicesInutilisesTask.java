package org.esup_portail.esup_stage.scheduler.SchedulableTasks;

import org.esup_portail.esup_stage.service.nettoyage.NettoyageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Supprime définitivement les services d'accueil qui n'ont plus aucun contact et qui ne sont
 * référencés ni par une convention ni par un avenant. À planifier après la suppression des
 * contacts inutilisés. Tâche inactive par défaut : la suppression est irréversible, chaque
 * établissement doit l'activer explicitement.
 */
@Component("SupprimerServicesInutilises")
public class SupprimerServicesInutilisesTask implements SchedulableTask {

    @Autowired
    private NettoyageService nettoyageService;

    @Override
    public Runnable getRunnable() {
        return () -> nettoyageService.supprimerServicesInutilises();
    }
}
