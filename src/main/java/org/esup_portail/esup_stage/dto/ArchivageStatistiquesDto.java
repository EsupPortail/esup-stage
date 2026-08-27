package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.CronTask;

import java.util.Date;

@Data
public class ArchivageStatistiquesDto {

    private long conventionsArchivees;

    // Conventions archivées dont les fichiers n'ont pas encore été déplacés vers le dossier d'archives
    private long conventionsFichiersATrier;

    // Conventions archivées depuis plus longtemps que le délai de purge : supprimées au prochain passage de la purge
    private long conventionsPurgeables;

    private long structuresArchivees;

    private CronTask tacheArchivage;

    private CronTask tachePurge;

    // Prochaines exécutions planifiées, calculées depuis les expressions cron (null si tâche inactive ou expression invalide)
    private Date prochaineExecutionArchivage;

    private Date prochaineExecutionPurge;
}
