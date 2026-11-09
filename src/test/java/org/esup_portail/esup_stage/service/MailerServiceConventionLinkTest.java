package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.model.Convention;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MailerServiceConventionLinkTest {

    private static final String EXPECTED = "https://esup.univ.fr/esup-stage/frontend/#/conventions/999";

    @Test
    void keepsUrlWithoutTrailingSlash() {
        assertThat(MailerService.buildConventionLink("https://esup.univ.fr/esup-stage", 999))
                .isEqualTo(EXPECTED);
    }

    @Test
    void removesOneTrailingSlash() {
        assertThat(MailerService.buildConventionLink("https://esup.univ.fr/esup-stage/", 999))
                .isEqualTo(EXPECTED);
    }

    @Test
    void removesSeveralTrailingSlashes() {
        assertThat(MailerService.buildConventionLink("https://esup.univ.fr/esup-stage///", 999))
                .isEqualTo(EXPECTED);
    }

    @Test
    void buildsTheLinkUsedInMailContext() {
        AppliProperties appliProperties = new AppliProperties();
        appliProperties.setUrl("https://esup.univ.fr/esup-stage/");
        Convention convention = new Convention();
        convention.setId(999);

        MailerService.MailContext.ConventionContext context =
                new MailerService.MailContext.ConventionContext(appliProperties, convention);

        assertThat(context.getLien()).isEqualTo(EXPECTED);
    }
}
