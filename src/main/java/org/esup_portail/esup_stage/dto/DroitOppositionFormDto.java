package org.esup_portail.esup_stage.dto;

import lombok.Data;

import java.util.List;

/**
 * Saisie en masse des refus d'être contacté remontés sur la boîte mail générique.
 */
@Data
public class DroitOppositionFormDto {

    private List<String> mails;
}
