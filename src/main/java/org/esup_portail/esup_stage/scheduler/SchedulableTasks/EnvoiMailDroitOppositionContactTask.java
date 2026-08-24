package org.esup_portail.esup_stage.scheduler.SchedulableTasks;

import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.dto.DroitOppositionResultDto;
import org.esup_portail.esup_stage.service.DroitOppositionContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Sollicite mensuellement les contacts en entreprise des conventions validées pour qu'ils puissent
 * signaler leur refus d'être contactés. Le nom du bean doit rester identique au champ
 * {@code CronTask.nom} : c'est ainsi que {@code CronScheduler} le résout.
 */
@Slf4j
@Component("EnvoiMailDroitOppositionContact")
public class EnvoiMailDroitOppositionContactTask implements SchedulableTask {

    public static final String NAME = "EnvoiMailDroitOppositionContact";
    public static final String DEFAULT_CRON_EXPRESSION = "0 0 3 1 * ?";

    @Autowired
    private DroitOppositionContactService droitOppositionContactService;

    @Override
    public void init() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getCronExpression() {
        return DEFAULT_CRON_EXPRESSION;
    }

    @Override
    public boolean isEnable() {
        return true;
    }

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
