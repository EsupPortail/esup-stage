package org.esup_portail.esup_stage.service.proprety;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ConfigMissingService {

    private static final List<String> REQUIRED_KEYS = List.of(
            "appli.url",
            "referentiel.ws.login",
            "referentiel.ws.password",
            "referentiel.ws.ldap_url",
            "referentiel.ws.apogee_url"
    );

    @Autowired
    private Environment environment;

    public List<String> getMissingKeys() {
        List<String> missing = new ArrayList<>();
        for (String key : REQUIRED_KEYS) {
            String value = environment.getProperty(key);
            if (!StringUtils.hasText(value)) {
                missing.add(key);
            }
        }
        return missing;
    }

    public boolean hasMissingKeys() {
        return !getMissingKeys().isEmpty();
    }

    public List<String> getRequiredKeys() {
        return Collections.unmodifiableList(REQUIRED_KEYS);
    }
}
