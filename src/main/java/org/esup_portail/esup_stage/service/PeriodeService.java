package org.esup_portail.esup_stage.service;

import org.springframework.stereotype.Service;

@Service
public class PeriodeService {

    public static final int NB_JOUR_MOIS = 22;

    /**
     * Calcul de la période en temps ouvré à partir d'un nombre d'heures par semaine et total
     * @param nbHeuresHebdo le nombre d'heures par semaine
     * @param nbHeures le nombre d'heure au total
     * @return Retourne sous forme "X mois Y jour(s) et Z heure(s)" en temps ouvré : 1 mois = 22 jours, 1 semaine = 5 jours
     */
    public static String calculPeriodeOuvree(float nbHeuresHebdo, int nbHeures) {
        int nbHeuresJournalieres = Math.round(nbHeuresHebdo / 5);  // nombre d'heures travaillées par jours

        final int nbJours = Math.floorDiv(nbHeures, nbHeuresJournalieres);
        final int nbHeuresRestantes = Math.floorMod(nbHeures, nbHeuresJournalieres);
        final int nbMois = Math.floorDiv(nbJours, NB_JOUR_MOIS);
        final int nbJoursRestants = Math.floorMod(nbJours, NB_JOUR_MOIS);

        return String.format("%s mois %s jour(s) %s heure(s)", nbMois, nbJoursRestants, nbHeuresRestantes);
    }
}
