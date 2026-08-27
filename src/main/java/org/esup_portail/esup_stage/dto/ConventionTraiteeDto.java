package org.esup_portail.esup_stage.dto;

import org.esup_portail.esup_stage.model.Convention;

import java.util.Date;

/**
 * Ligne du rapport des conventions effectivement traitées par un archivage ou une purge.
 * Les infos sont capturées pendant le traitement — indispensable pour la purge, qui
 * supprime définitivement les conventions.
 */
public record ConventionTraiteeDto(
        Integer id,
        String nomEtudiant,
        String prenomEtudiant,
        String structure,
        String annee,
        Date dateFinStage,
        boolean gratification,
        Date dateArchivage
) {

    public static ConventionTraiteeDto from(Convention convention) {
        boolean gratification = Boolean.TRUE.equals(convention.getGratificationStage())
                || (convention.getMontantGratification() != null && !convention.getMontantGratification().isEmpty());
        return new ConventionTraiteeDto(
                convention.getId(),
                convention.getEtudiant() != null ? convention.getEtudiant().getNom() : null,
                convention.getEtudiant() != null ? convention.getEtudiant().getPrenom() : null,
                convention.getStructure() != null ? convention.getStructure().getRaisonSociale() : null,
                convention.getAnnee(),
                convention.getDateFinStage(),
                gratification,
                convention.getDateArchivage()
        );
    }
}
