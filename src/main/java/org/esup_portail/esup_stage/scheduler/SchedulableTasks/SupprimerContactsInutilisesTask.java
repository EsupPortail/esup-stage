package org.esup_portail.esup_stage.scheduler.SchedulableTasks;

import org.esup_portail.esup_stage.service.nettoyage.NettoyageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Supprime définitivement les contacts qui ne sont plus référencés par aucune donnée
 * (convention, avenant, offre, accord de partenariat, token d'évaluation). Tâche inactive par
 * défaut : la suppression est irréversible, chaque établissement doit l'activer explicitement.
 */
@Component("SupprimerContactsInutilises")
public class SupprimerContactsInutilisesTask implements SchedulableTask {

    @Autowired
    private NettoyageService nettoyageService;

    @Override
    public Runnable getRunnable() {
        return () -> nettoyageService.supprimerContactsInutilises();
    }
}
