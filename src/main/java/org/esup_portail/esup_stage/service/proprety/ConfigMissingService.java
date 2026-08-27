package org.esup_portail.esup_stage.service.proprety;

import org.esup_portail.esup_stage.model.AppProperty;
import org.esup_portail.esup_stage.repository.AppPropertyJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ConfigMissingService {

    private static final List<String> REQUIRED_KEYS = List.of(
            "referentiel.ws.login",
            "referentiel.ws.password",
            "referentiel.ws.ldap_url",
            "referentiel.ws.apogee_url"
    );
    private static final Set<String> SECRET_REQUIRED_KEYS = Set.of(
            "referentiel.ws.password"
    );

    @Autowired
    private Environment environment;

    @Autowired
    private AppPropertyJpaRepository appPropertyJpaRepository;

    public List<String> getMissingKeys() {
        List<String> missing = new ArrayList<>();
        for (String key : REQUIRED_KEYS) {
            if (SECRET_REQUIRED_KEYS.contains(key)) {
                if (!hasSecretValue(key) && !StringUtils.hasText(environment.getProperty(key))) {
                    missing.add(key);
                }
                continue;
            }
            String value = environment.getProperty(key);
            if (!StringUtils.hasText(value)) {
                missing.add(key);
            }
        }
        return missing;
    }

    private boolean hasSecretValue(String key) {
        AppProperty prop = appPropertyJpaRepository.findByKey(key);
        if (prop == null) {
            return false;
        }
        return StringUtils.hasText(prop.getValueEncrypted()) || StringUtils.hasText(prop.getValue());
    }

    public boolean hasMissingKeys() {
        return !getMissingKeys().isEmpty();
    }

}
