package org.esup_portail.esup_stage.constants;

// En attente de la classe ValidationPatterns, portée par une autre branche.
/*
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationPatternsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "simple@example.com",
            "prenom.nom@univ-lorraine.fr",
            "avec+tag@domaine.org",
            "chiffres123@sub.domaine.co",
            "tres.longue.partie.locale.qui.depasse.soixante.quatre.caracteres.autorisee@domaine.fr",
    })
    void accepteLesAdressesValides(String email) {
        assertThat(email.matches(ValidationPatterns.EMAIL)).as("%s doit être acceptée", email).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "sansarobase.fr",
            "double..point@domaine.fr",
            "@domaine.fr",
            "utilisateur@",
            "utilisateur@domaine",
            "utilisateur@domaine.f",
            "espace interdit@domaine.fr",
    })
    void rejetteLesAdressesInvalides(String email) {
        assertThat(email.matches(ValidationPatterns.EMAIL)).as("%s doit être rejetée", email).isFalse();
    }
}
*/
