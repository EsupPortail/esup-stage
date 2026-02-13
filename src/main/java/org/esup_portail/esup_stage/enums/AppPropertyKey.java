package org.esup_portail.esup_stage.enums;

public enum AppPropertyKey {

    // === APPLI ===
    APPLI_URL("appli.url"),
    APPLI_TOKENS("appli.tokens"),
    APPLI_JWT_SECRET("appli.jwt_secret"),
    APPLI_NB_JOURS_VALIDE_TOKEN("appli.nb_jours_valide_token"),

    // === MAILER ===
    MAILER_PROTOCOL("appli.mailer.protocol"),
    MAILER_HOST("appli.mailer.host"),
    MAILER_PORT("appli.mailer.port"),
    MAILER_AUTH("appli.mailer.auth"),
    MAILER_USERNAME("appli.mailer.username"),
    MAILER_PASSWORD("appli.mailer.password"),
    MAILER_FROM("appli.mailer.from"),
    MAILER_DISABLE_DELIVERY("appli.mailer.disable_delivery"),
    MAILER_DELIVERY_ADDRESS("appli.mailer.delivery_address"),

    // === REFERENTIEL WS ===
    REFERENTIEL_WS_LOGIN("referentiel.ws.login"),
    REFERENTIEL_WS_PASSWORD("referentiel.ws.password"),
    REFERENTIEL_WS_LDAP_URL("referentiel.ws.ldap_url"),
    REFERENTIEL_WS_APOGEE_URL("referentiel.ws.apogee_url"),

    // === WEBHOOK ===
    WEBHOOK_SIGNATURE_URI("webhook.signature.uri"),
    WEBHOOK_SIGNATURE_TOKEN("webhook.signature.token"),

    // === SIRENE ===
    SIRENE_URL("sirene.url"),
    SIRENE_TOKEN("sirene.token"),
    SIRENE_NOMBRE_MINIMUM_RESULTATS("sirene.nombre_minimum_resultats"),

    // === FOOTER ===
    FOOTER_GITHUB("appli.footer.github"),
    FOOTER_SITE("appli.footer.site"),
    FOOTER_SUPPORT("appli.footer.support"),
    FOOTER_WIKI("appli.footer.wiki");

    private final String key;

    AppPropertyKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    @Override
    public String toString() {
        return key;
    }
}
