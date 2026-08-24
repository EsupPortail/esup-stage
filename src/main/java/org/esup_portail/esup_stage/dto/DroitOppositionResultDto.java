package org.esup_portail.esup_stage.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Compte-rendu d'un traitement de droit d'opposition : saisie en masse des refus ou envoi
 * automatique des mails aux contacts en entreprise.
 */
@Data
public class DroitOppositionResultDto {

    /** Adresses reconnues, avec le nombre de contacts impactés */
    private List<AdresseTraitee> traitees = new ArrayList<>();

    /** Adresses valides mais ne correspondant à aucun contact */
    private List<String> inconnues = new ArrayList<>();

    /** Adresses rejetées car mal formées */
    private List<String> invalides = new ArrayList<>();

    /** Nombre de mails effectivement envoyés (envoi automatique) */
    private int envoyes;

    /** Nombre de mails en échec (envoi automatique) */
    private int erreurs;

    @Data
    public static class AdresseTraitee {
        private String mail;
        private int nbContacts;

        public AdresseTraitee(String mail, int nbContacts) {
            this.mail = mail;
            this.nbContacts = nbContacts;
        }
    }

    public void ajouterTraitee(String mail, int nbContacts) {
        traitees.add(new AdresseTraitee(mail, nbContacts));
    }
}
