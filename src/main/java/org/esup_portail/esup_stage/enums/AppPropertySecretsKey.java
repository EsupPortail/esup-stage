package org.esup_portail.esup_stage.enums;

public enum AppPropertySecretsKey {
    APPLI_JWT_SECRET("appli.jwt_secret"),
    APPLI_MAILER_PASSWORD("appli.mailer.password"),
    REFERENTIEL_WS_PASSWORD("referentiel.ws.password"),
    WEBHOOK_SIGNATURE_TOKEN("webhook.signature.token"),
    SIRENE_TOKEN("sirene.token"),
    DOCAPOSTE_KEYSTORE_PASSWORD("docaposte.keystore.password"),
    DOCAPOSTE_TRUSTSTORE_PASSWORD("docaposte.truststore.password");

    private final String key;

    AppPropertySecretsKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static boolean isSecret(String key) {
        for (AppPropertySecretsKey sk : values()) {
            if (sk.key.equals(key)) return true;
        }
        return false;
    }
}
