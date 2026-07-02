package org.esup_portail.esup_stage.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConventionServiceTest {

    private final ConventionService conventionService = new ConventionService();

    @Test
    void parseNumTelKeepsValidFixedLineNumbersForGeneralUse() {
        assertThat(conventionService.parseNumTel("0182280026")).isEqualTo("+33182280026");
    }

    @Test
    void parseNumTelMobileRejectsFixedLineNumbersRejectedByDocaposteOtp() {
        assertThat(conventionService.parseNumTelMobile("0182280026")).isNull();
        assertThat(conventionService.parseNumTelMobile("0596782950")).isNull();
        assertThat(conventionService.parseNumTelMobile("0188321350")).isNull();
    }

    @Test
    void parseNumTelMobileFormatsValidMobileNumbers() {
        assertThat(conventionService.parseNumTelMobile("0612345678")).isEqualTo("+33612345678");
    }
}
