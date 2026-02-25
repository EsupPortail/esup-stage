package org.esup_portail.esup_stage.service.proprety;

import org.esup_portail.esup_stage.model.AppProperty;
import org.esup_portail.esup_stage.repository.AppPropertyJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppProperyService {

    @Autowired
    private AppPropertyJpaRepository appPropertyJpaRepository;

    public List<AppProperty> getAll() {
        return appPropertyJpaRepository.findAll();
    }

    Map<String, String> getOverrides() {
        List<AppProperty> props = appPropertyJpaRepository.findAll();
        Map<String, String> overrides = new LinkedHashMap<>();
        for (AppProperty prop : props) {
            if (prop == null) {
                continue;
            }
            String key = prop.getKey();
            String value = prop.getValue();
            if (!StringUtils.hasText(key)) {
                continue;
            }
            if (!StringUtils.hasText(value)) {
                continue; // null/blank => do not override .properties
            }
            overrides.put(key, value);
        }
        return overrides;
    }

    public void save(String key, String value, String user) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        AppProperty existing = appPropertyJpaRepository.findByKey(key);
        AppProperty toSave = existing != null ? existing : new AppProperty();
        toSave.setKey(key);
        toSave.setValue(value);
        String now = Instant.now().toString();
        toSave.setUpdateAt(now);
        if (!StringUtils.hasText(toSave.getCreatedAt())) {
            toSave.setCreatedAt(now);
        }
        appPropertyJpaRepository.save(toSave);
    }
}
