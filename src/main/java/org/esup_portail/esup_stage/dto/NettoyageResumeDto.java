package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.CronTask;

import java.util.Date;

/**
 * État des tâches planifiées de nettoyage (contacts et services), pour la page
 * d'administration de l'archivage. Les compteurs d'inutilisés sont fournis à part
 * (dénombrement coûteux, chargé à l'ouverture de l'onglet).
 */
@Data
public class NettoyageResumeDto {

    private CronTask tacheContacts;
    private CronTask tacheServices;

    // Prochaines exécutions planifiées, calculées depuis les expressions cron (null si inactive)
    private Date prochaineExecutionContacts;
    private Date prochaineExecutionServices;
}
