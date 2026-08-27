package org.esup_portail.esup_stage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodeServiceTest {

    @Test
    void unMoisCompletDe22JoursOuvres() {
        // 35 h/semaine = 7 h/jour ; 154 h = 22 jours = 1 mois pile
        assertThat(PeriodeService.calculPeriodeOuvree(35f, 154f))
                .isEqualTo("1 mois 0 jour(s) 0 heure(s)");
    }

    @ParameterizedTest
    @CsvSource({
            "35, 10,  '0 mois 1 jour(s) 3 heure(s)'",   // 10 h = 1 jour de 7 h + 3 h
            "35, 7,   '0 mois 1 jour(s) 0 heure(s)'",
            "35, 3,   '0 mois 0 jour(s) 3 heure(s)'",
            "35, 161, '1 mois 1 jour(s) 0 heure(s)'",   // 23 jours
            "35, 308, '2 mois 0 jour(s) 0 heure(s)'",   // 44 jours
            "20, 44,  '0 mois 11 jour(s) 0 heure(s)'",  // 4 h/jour
    })
    void calculeLaPeriodeOuvree(float nbHeuresHebdo, float nbHeures, String attendu) {
        assertThat(PeriodeService.calculPeriodeOuvree(nbHeuresHebdo, nbHeures)).isEqualTo(attendu);
    }

    @Test
    void nombreDeJoursParMoisEstConforme() {
        assertThat(PeriodeService.NB_JOUR_MOIS).isEqualTo(22);
    }
}
