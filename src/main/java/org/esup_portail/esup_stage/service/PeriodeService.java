package org.esup_portail.esup_stage.service;

import org.springframework.stereotype.Service;

@Service
public class PeriodeService {

    public static final int NB_JOUR_MOIS = 22;

    /**
     * Calcul de la période en temps ouvré à partir d'un nombre d'heures par semaine et total
     *
     * @param nbHeuresHebdo le nombre d'heures par semaine
     * @param nbHeures      le nombre d'heures au total
     * @return Retourne sous forme "X mois Y jour(s) et Z heure(s)" en temps ouvré : 1 mois = 22 jours, 1 semaine = 5 jours
     */
    public static String calculPeriodeOuvree(float nbHeuresHebdo, float nbHeures) {
        float nbHeuresJournalieres = nbHeuresHebdo / 5;  // nombre d'heures travaillées par jours

        final int nbJours = (int) (nbHeures / nbHeuresJournalieres);
        final int nbMois = nbJours / NB_JOUR_MOIS;
        final int nbJoursRestants = nbJours % NB_JOUR_MOIS;
        final int nbHeuresRestantes = Math.round(nbHeures - ((nbMois * NB_JOUR_MOIS + nbJoursRestants) * nbHeuresJournalieres));

        return String.format("%s mois %s jour(s) %s heure(s)", nbMois, nbJoursRestants, nbHeuresRestantes);
    }
}
