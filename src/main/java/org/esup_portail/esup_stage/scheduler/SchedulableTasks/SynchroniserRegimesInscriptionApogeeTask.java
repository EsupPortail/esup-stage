package org.esup_portail.esup_stage.scheduler.SchedulableTasks;

import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.service.apogee.RegimeInscriptionApogeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("SynchroniserRegimesInscriptionApogee")
public class SynchroniserRegimesInscriptionApogeeTask implements SchedulableTask {

    public static final String NAME = "SynchroniserRegimesInscriptionApogee";
    public static final String DEFAULT_CRON_EXPRESSION = "0 0 2 * * ?";

    @Autowired
    private RegimeInscriptionApogeeService regimeInscriptionApogeeService;

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
            int nbRegimesActifs = regimeInscriptionApogeeService.synchroniserDepuisApogee().size();
            log.info("Synchronisation des regimes d'inscription Apogee terminee : {} regimes actifs", nbRegimesActifs);
        };
    }
}
