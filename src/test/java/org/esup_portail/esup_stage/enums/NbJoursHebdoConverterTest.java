package org.esup_portail.esup_stage.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NbJoursHebdoConverterTest {

    private final NbJoursHebdoConverter converter = new NbJoursHebdoConverter();

    @Test
    void convertitChaqueConstanteVersSaValeurEnBase() {
        for (NbJoursHebdoEnum value : NbJoursHebdoEnum.values()) {
            assertThat(converter.convertToDatabaseColumn(value)).isEqualTo(value.getValue());
        }
    }

    @Test
    void convertitNullVersNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void relitChaqueValeurDeBaseVersLaConstante() {
        for (NbJoursHebdoEnum value : NbJoursHebdoEnum.values()) {
            assertThat(converter.convertToEntityAttribute(value.getValue())).isEqualTo(value);
        }
    }

    @Test
    void rejetteUneValeurInconnue() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("42"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allerRetourSurUnExemple() {
        assertThat(converter.convertToEntityAttribute(converter.convertToDatabaseColumn(NbJoursHebdoEnum.DEUX_CINQ)))
                .isEqualTo(NbJoursHebdoEnum.DEUX_CINQ);
    }
}
