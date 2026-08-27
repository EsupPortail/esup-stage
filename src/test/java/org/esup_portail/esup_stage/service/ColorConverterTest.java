package org.esup_portail.esup_stage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ColorConverterTest {

    @ParameterizedTest
    @CsvSource({
            "0, 100, 50, 'rgb(255, 0, 0)'",      // rouge pur
            "120, 100, 50, 'rgb(0, 255, 0)'",    // vert pur
            "240, 100, 50, 'rgb(0, 0, 255)'",    // bleu pur
            "60, 100, 50, 'rgb(255, 255, 0)'",   // jaune
            "180, 100, 50, 'rgb(0, 255, 255)'",  // cyan
            "300, 100, 50, 'rgb(255, 0, 255)'",  // magenta
            "0, 0, 0, 'rgb(0, 0, 0)'",           // noir
            "0, 0, 100, 'rgb(255, 255, 255)'",   // blanc
    })
    void convertitLesCouleursHslDeReference(int h, int s, int l, String rgb) {
        assertThat(ColorConverter.hslToRgb(h, s, l)).isEqualTo(rgb);
    }

    @Test
    void remplaceLesCouleursHslDansDuHtml() {
        String html = "<span style=\"color:hsl(0, 100%, 50%)\">texte</span>";

        String converti = ColorConverter.convertHslToRgb(html);

        assertThat(converti).isEqualTo("<span style=\"color:rgb(255, 0, 0)\">texte</span>");
    }

    @Test
    void remplaceToutesLesOccurrences() {
        String html = "color:hsl(120, 100%, 50%) puis color:hsl(240, 100%, 50%)";

        String converti = ColorConverter.convertHslToRgb(html);

        assertThat(converti).isEqualTo("color:rgb(0, 255, 0) puis color:rgb(0, 0, 255)");
    }

    @Test
    void laisseLeHtmlSansHslInchange() {
        String html = "<p style=\"color:rgb(1, 2, 3)\">déjà en rgb</p>";

        assertThat(ColorConverter.convertHslToRgb(html)).isEqualTo(html);
    }
}
