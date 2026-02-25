package org.esup_portail.esup_stage.config.properties;

import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class DbPropertySource extends MapPropertySource {
    public DbPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }
}
