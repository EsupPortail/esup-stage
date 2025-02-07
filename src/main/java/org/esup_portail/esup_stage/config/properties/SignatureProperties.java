package org.esup_portail.esup_stage.config.properties;

import lombok.Data;
import org.esup_portail.esup_stage.config.properties.signature.DocaposteProperties;
import org.esup_portail.esup_stage.config.properties.signature.EsupSignatureProperties;
import org.esup_portail.esup_stage.config.properties.signature.WebhookProperties;
import org.esup_portail.esup_stage.enums.AppSignatureEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Data
@Configuration
public class SignatureProperties {

    private final DocaposteProperties docaposte;
    private final WebhookProperties webhook;
    private final EsupSignatureProperties esupsignature;
    private AppSignatureEnum appSignatureType;

    @Autowired
    public SignatureProperties(DocaposteProperties docaposte,
                               WebhookProperties webhook,
                               EsupSignatureProperties esupsignature) {
        this.docaposte = docaposte;
        this.webhook = webhook;
        this.esupsignature = esupsignature;
        initializeProperties();
    }

    public void initializeProperties() {
        boolean docaposteEnabled = docaposte.getUri() != null && docaposte.getSiren() != null &&
                docaposte.getKeystorePath() != null && docaposte.getKeystorePassword() != null &&
                docaposte.getTruststorePath() != null && docaposte.getTruststorePassword() != null;

        if (docaposteEnabled) {
            appSignatureType = AppSignatureEnum.DOCAPOSTE;
        } else {
            if (StringUtils.hasText(webhook.getUri()) && StringUtils.hasText(webhook.getToken())) {
                appSignatureType = AppSignatureEnum.EXTERNE;
                if (StringUtils.hasText(esupsignature.getUri())) {
                    appSignatureType = AppSignatureEnum.ESUPSIGNATURE;
                }
            }
        }
    }
}
