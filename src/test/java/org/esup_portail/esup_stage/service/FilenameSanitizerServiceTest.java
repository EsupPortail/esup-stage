package org.esup_portail.esup_stage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class FilenameSanitizerServiceTest {

    private final FilenameSanitizerService service = new FilenameSanitizerService();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "///", "???", "___", "..."})
    void remplaceLesNomsInexploitablesParDocument(String filename) {
        assertThat(service.sanitize(filename)).isEqualTo("document");
    }

    @ParameterizedTest
    @CsvSource({
            "'convention étudiant.pdf', 'convention_etudiant.pdf'",
            "'Rapport Final', 'Rapport_Final'",
            "'a/b\\c:d*e', 'a_b_c_d_e'",
            "'  spaces  ', 'spaces'",
            "'__x__', 'x'",
            "'çàéèùï', 'caeeui'",
            "'nom.avec.points', 'nom.avec.points'",
    })
    void nettoieLesCaracteresDangereux(String entree, String attendu) {
        assertThat(service.sanitize(entree)).isEqualTo(attendu);
    }

    @Test
    void tronqueLesNomsTropLongs() {
        String longName = "a".repeat(200);

        String sanitized = service.sanitize(longName);

        assertThat(sanitized).hasSize(120);
        assertThat(sanitized).matches("a+");
    }

    @Test
    void prefixeTemporaireFaitAuMoinsTroisCaracteres() {
        assertThat(service.sanitizeTempFilePrefix("a")).isEqualTo("a__");
        assertThat(service.sanitizeTempFilePrefix("ab")).isEqualTo("ab_");
        assertThat(service.sanitizeTempFilePrefix("abc")).isEqualTo("abc");
        assertThat(service.sanitizeTempFilePrefix("abcdef")).isEqualTo("abcdef");
        assertThat(service.sanitizeTempFilePrefix(null)).isEqualTo("document");
    }
}
