package org.esup_portail.esup_stage.config.properties;

import jakarta.annotation.PostConstruct;
import org.esup_portail.esup_stage.config.properties.signature.DocaposteProperties;
import org.esup_portail.esup_stage.config.properties.signature.EsupSignatureProperties;
import org.esup_portail.esup_stage.config.properties.signature.WebhookProperties;
import org.esup_portail.esup_stage.service.proprety.AppProperyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DbPropertySourceRegistrar {

    @Autowired
    private AppProperyService appProperyService;

    @Autowired
    private AppliProperties appliProperties;

    @Autowired
    private ReferentielProperties referentielProperties;

    @Autowired
    private SireneProperties sireneProperties;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private CasProperties casProperties;

    @Autowired
    private DocaposteProperties docaposteProperties;

    @Autowired
    private EsupSignatureProperties esupSignatureProperties;

    @Autowired
    private WebhookProperties webhookProperties;

    @Autowired
    private SignatureProperties signatureProperties;

    private final ConfigurableEnvironment env;

    public DbPropertySourceRegistrar(ConfigurableEnvironment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    @EventListener
    public void onConfigReload(ConfigReloadEvent event) {
        refresh();
    }

    public void refresh() {
        Map<String, String> overrides = appProperyService.getOverrides();
        Map<String, Object> source = new HashMap<>(overrides);
        DbPropertySource ps = new DbPropertySource("dbOverrides", source);

        MutablePropertySources sources = env.getPropertySources();
        if (sources.contains("dbOverrides")) {
            sources.replace("dbOverrides", ps);
        } else {
            sources.addFirst(ps);
        }

        rebindConfigurationProperties();
    }

    private void rebindConfigurationProperties() {
        Binder binder = Binder.get(env);
        binder.bind("appli", Bindable.ofInstance(appliProperties));
        binder.bind("referentiel.ws", Bindable.ofInstance(referentielProperties));
        binder.bind("sirene", Bindable.ofInstance(sireneProperties));
        binder.bind("application", Bindable.ofInstance(applicationProperties));
        binder.bind("cas", Bindable.ofInstance(casProperties));
        binder.bind("docaposte", Bindable.ofInstance(docaposteProperties));
        binder.bind("esupsignature", Bindable.ofInstance(esupSignatureProperties));
        binder.bind("webhook.signature", Bindable.ofInstance(webhookProperties));
        signatureProperties.initializeProperties();
    }
}
