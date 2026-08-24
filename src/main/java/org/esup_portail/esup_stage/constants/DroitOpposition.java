package org.esup_portail.esup_stage.constants;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Constantes du droit d'opposition des contacts en entreprise.
 * <p>
 * Volontairement hors des entités : {@code GroupeEtudiantController.mergeObjects} parcourt tous les
 * champs déclarés de {@code Convention} par réflexion et appelle {@code Field#set}, ce qui échoue
 * sur un champ {@code static final}.
 */
public final class DroitOpposition {

    private DroitOpposition() {
    }

    /** Mention substituée aux coordonnées d'un contact ayant exercé son droit d'opposition */
    public static final String MENTION_REFUS_ETRE_CONTACTE = "Ne souhaite pas être contacté";

    /** Refus saisi à la main sur un écran contact / tuteur pro / signataire */
    public static final String ORIGINE_REFUS_MANUEL = "MANUEL";

    /** Refus enregistré depuis l'écran de saisie en masse des retours de la boîte générique */
    public static final String ORIGINE_REFUS_MASSE = "MASSE";

    private static final String OBJET_MAILTO = "Je ne souhaite pas être contacté / I do not wish to be contacted";

    /**
     * Lien {@code mailto:} pré-rempli vers la boîte générique, injecté dans les templates de mail
     * via {@code ${lienOpposition}}. Le corps rappelle l'identité du contact pour que le
     * gestionnaire retrouve la fiche à mettre à jour à réception.
     */
    public static String construireLienMailto(String mailGenerique, String prenom, String nom, String mail) {
        String corps = "Bonjour,\n\n"
                + "Je ne souhaite pas être contacté par votre établissement.\n"
                + "I do not wish to be contacted by your institution.\n\n"
                + "Contact : " + prenom + " " + nom + " (" + mail + ")\n";
        return "mailto:" + mailGenerique
                + "?subject=" + encode(OBJET_MAILTO)
                + "&body=" + encode(corps);
    }

    private static String encode(String value) {
        // URLEncoder encode l'espace en "+", non interprété dans un mailto:
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
