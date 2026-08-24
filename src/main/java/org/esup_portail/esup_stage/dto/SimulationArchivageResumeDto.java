package org.esup_portail.esup_stage.dto;

import lombok.Data;

import java.util.Date;

/**
 * Récapitulatif de la simulation : ce que traiteraient les tâches d'archivage et de purge
 * si elles s'exécutaient maintenant, avec les dates seuils calculées depuis la configuration.
 */
@Data
public class SimulationArchivageResumeDto {

    private long conventionsAArchiver;
    private long conventionsAArchiverSansGratification;
    private long conventionsAArchiverAvecGratification;
    private long conventionsAPurger;

    private Date seuilArchivageSansGratification;
    private Date seuilArchivageAvecGratification;
    private Date seuilPurge;
}
