package org.esup_portail.esup_stage.docaposte.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.enums.AppSignatureEnum;
import org.esup_portail.esup_stage.service.ConventionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DocaposteBatch {

    private static final Logger logger	= LogManager.getLogger(DocaposteBatch.class);

    @Autowired
    ConventionService conventionService;

    @Autowired
    SignatureProperties signatureProperties;

    /**
     * Exécution tous les jours à 1h
     */
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void updateSignatureInfo() {
        logger.info("Start getting signature information");
        AppSignatureEnum appSignature = signatureProperties.getAppSignatureType();
        if (appSignature == null) {
            throw new RuntimeException("La signature électronique n'est pas configurée");
        }
        if (appSignature == AppSignatureEnum.EXTERNE) {
            logger.info("La récupération de l'historique sera faite automatiquement");
        } else {
            conventionService.updateSignatureElectroniqueHistorique();
        }
        logger.info("End getting signature information");
    }
}
