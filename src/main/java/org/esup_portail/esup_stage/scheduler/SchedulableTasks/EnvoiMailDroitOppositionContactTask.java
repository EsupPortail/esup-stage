package org.esup_portail.esup_stage.scheduler.SchedulableTasks;

import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.dto.DroitOppositionResultDto;
import org.esup_portail.esup_stage.service.DroitOppositionContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Sollicite les contacts en entreprise des conventions validées pour qu'ils puissent signaler leur
 * refus d'être contactés. Un contact n'est sollicité qu'une seule fois : chaque exécution ne traite
 * que les contacts nouvellement éligibles. Tâche inactive par défaut, elle suppose que la boîte
 * mail générique de recueil des refus soit renseignée dans les paramètres généraux.
 */
@Slf4j
@Component("EnvoiMailDroitOppositionContact")
public class EnvoiMailDroitOppositionContactTask implements SchedulableTask {

    @Autowired
    private DroitOppositionContactService droitOppositionContactService;

    @Override
    public Runnable getRunnable() {
        return () -> {
            try {
                DroitOppositionResultDto result = droitOppositionContactService.envoyerMailsDroitOpposition();
                log.info("Envoi des mails de droit d'opposition termine : {} envoye(s), {} en erreur",
                        result.getEnvoyes(), result.getErreurs());
            } catch (Exception e) {
                log.error("Echec de l'envoi des mails de droit d'opposition", e);
            }
        };
    }
}
