package org.esup_portail.esup_stage.config;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.StringWriter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MailerConfigurationTest {

    @Test
    void freemarkerConfigurationDisablesApiBuiltinAndUnsafeNewBuiltin() throws Exception {
        MailerConfiguration mailerConfiguration = new MailerConfiguration(appliProperties());
        FreeMarkerConfigurer configurer = mailerConfiguration.freemarkerClassLoaderConfig();
        Configuration configuration = configurer.getConfiguration();

        assertThat(configuration.isAPIBuiltinEnabled()).isFalse();

        Template apiTemplate = new Template("api-ssti", "${value?api.class}", configuration);
        assertThatThrownBy(() -> apiTemplate.process(Map.of("value", "test"), new StringWriter()))
                .hasMessageContaining("?api");

        Template newTemplate = new Template("new-ssti", "${\"freemarker.template.utility.Execute\"?new}", configuration);
        assertThatThrownBy(() -> newTemplate.process(Map.of(), new StringWriter()))
                .hasMessageContaining("not allowed");
    }

    private AppliProperties appliProperties() {
        AppliProperties appliProperties = new AppliProperties();
        AppliProperties.MailerProperties mailerProperties = new AppliProperties.MailerProperties();
        mailerProperties.setProtocol("smtp");
        appliProperties.setMailer(mailerProperties);
        return appliProperties;
    }
}
