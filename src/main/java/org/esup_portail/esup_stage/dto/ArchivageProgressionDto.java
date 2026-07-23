package org.esup_portail.esup_stage.dto;

import lombok.Data;

import java.util.Date;

/**
 * État d'avancement du traitement d'archivage/purge lancé manuellement depuis la page
 * d'administration : interrogé en polling par le frontend pour la barre de progression.
 */
@Data
public class ArchivageProgressionDto {

    private boolean enCours;

    // "Archivage" ou "Purge"
    private String tache;

    // Libellé de l'étape courante (ex. "Tri des fichiers")
    private String etape;

    private long traitees;

    // 0 = étape sans décompte (barre indéterminée)
    private long total;

    private Date dateDebut;
    private Date dateFin;

    // Bilan de fin de traitement, ou message d'erreur
    private String message;
    private boolean erreur;

    // Vrai si le traitement s'est arrêté suite à une demande d'annulation
    private boolean annule;

    // Rapport des conventions traitées disponible à l'export Excel, et son volume
    private boolean rapportDisponible;
    private long rapportNbLignes;
}
