package org.esup_portail.esup_stage.docaposte.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.service.ConventionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DocaposteBatch {

    private static final Logger logger	= LogManager.getLogger(DocaposteBatch.class);

    @Autowired
    ConventionService conventionService;

    /**
     * Exécution tous les jours à 1h
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void updateSignatureInfo() {
        logger.info("Start getting signature information from Docaposte");
        conventionService.updateSignatureElectroniqueHistorique();
        logger.info("End getting signature information from Docaposte");
    }
}
