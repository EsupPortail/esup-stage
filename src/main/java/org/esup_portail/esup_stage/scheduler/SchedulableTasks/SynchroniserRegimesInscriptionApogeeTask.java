package org.esup_portail.esup_stage.scheduler.SchedulableTasks;

import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.service.apogee.RegimeInscriptionApogeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("SynchroniserRegimesInscriptionApogee")
public class SynchroniserRegimesInscriptionApogeeTask implements SchedulableTask {

    @Autowired
    private RegimeInscriptionApogeeService regimeInscriptionApogeeService;

    @Override
    public Runnable getRunnable() {
        return () -> {
            int nbRegimesActifs = regimeInscriptionApogeeService.synchroniserDepuisApogee().size();
            log.info("Synchronisation des regimes d'inscription Apogee terminee : {} regimes actifs", nbRegimesActifs);
        };
    }
}
