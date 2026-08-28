package org.esup_portail.esup_stage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * État de la traduction des libellés d'impression pour une langue de convention.
 */
@Data
@NoArgsConstructor
public class LibelleImpressionLangueDto {

    private String code;
    private String libelle;

    /** Une surcharge a été déposée par l'établissement pour cette langue. */
    private boolean surcharge;

    /** Date de dernière modification du fichier de surcharge, null s'il n'y en a pas. */
    private Date dateModification;

    /** Nombre de libellés effectivement traduits pour cette langue. */
    private int nbClesRenseignees;

    /** Nombre total de libellés traduisibles. */
    private int nbClesTotal;
}
