package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.model.TypeConvention;
import org.esup_portail.esup_stage.repository.TypeConventionJpaRepository;
import org.esup_portail.esup_stage.service.apogee.model.RegimeInscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TypeConventionResolverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeConventionResolverService.class);

    @Autowired
    private TypeConventionJpaRepository typeConventionJpaRepository;

    private volatile boolean regimeAssociationQueryEnabled = true;

    public TypeConvention resolveTypeConventionByRegimeInscription(RegimeInscription regimeInscription) {
        return resolveTypeConventionByRegimeInscription(regimeInscription, null);
    }

    public TypeConvention resolveTypeConventionByRegimeInscription(RegimeInscription regimeInscription, String context) {
        List<TypeConvention> candidates = resolveTypeConventionsByRegimeInscription(regimeInscription, context);
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    public List<TypeConvention> resolveTypeConventionsByRegimeInscription(RegimeInscription regimeInscription) {
        return resolveTypeConventionsByRegimeInscription(regimeInscription, null);
    }

    public List<TypeConvention> resolveTypeConventionsByRegimeInscription(RegimeInscription regimeInscription, String context) {
        if (regimeInscription == null) {
            return Collections.emptyList();
        }

        String codSisRegIns = normalize(regimeInscription.getCodSisRegIns());
        String codRegIns = normalize(regimeInscription.getCodRegIns());
        String licRegIns = normalize(regimeInscription.getLicRegIns());

        Set<String> primaryCodes = new LinkedHashSet<>();
        if (hasText(codSisRegIns)) {
            primaryCodes.add(codSisRegIns);
        }
        if (hasText(codRegIns)) {
            primaryCodes.add(codRegIns);
        }

        for (String code : primaryCodes) {
            List<TypeConvention> candidates = resolveTypeConventionsByCode(code);
            if (!candidates.isEmpty()) {
                return candidates;
            }
        }

        // Fallback transitoire sur LIC_RGI uniquement
        if (hasText(licRegIns)) {
            List<TypeConvention> candidates = resolveTypeConventionsByCode(licRegIns);
            if (!candidates.isEmpty()) {
                LOGGER.warn(
                        "Fallback transitoire LIC_RGI utilise pour la resolution du type de convention{} (codSisRegIns='{}', codRegIns='{}', licRegIns='{}')",
                        buildContext(context),
                        codSisRegIns,
                        codRegIns,
                        licRegIns
                );
                return candidates;
            }
        }

        return Collections.emptyList();
    }

    private List<TypeConvention> resolveTypeConventionsByCode(String code) {
        if (!hasText(code)) {
            return Collections.emptyList();
        }

        Map<Integer, TypeConvention> unique = new LinkedHashMap<>();

        for (TypeConvention typeConvention : findByRegimeCode(code)) {
            if (typeConvention != null) {
                unique.putIfAbsent(typeConvention.getId(), typeConvention);
            }
        }

        if (unique.isEmpty()) {
            TypeConvention legacy = typeConventionJpaRepository.findByCodeCtrl(code);
            if (legacy != null) {
                unique.putIfAbsent(legacy.getId(), legacy);
            }
        }

        return new ArrayList<>(unique.values());
    }

    private List<TypeConvention> findByRegimeCode(String code) {
        if (!regimeAssociationQueryEnabled) {
            return Collections.emptyList();
        }

        try {
            return typeConventionJpaRepository.findAllByRegimeCode(code);
        } catch (DataAccessException | IllegalArgumentException e) {
            regimeAssociationQueryEnabled = false;
            LOGGER.warn(
                    "Lookup TypeConventionRegimeInscription desactive (table/requete indisponible). Fallback legacy codeCtrl uniquement. Cause: {}",
                    e.getClass().getSimpleName()
            );
            return Collections.emptyList();
        }
    }

    private String buildContext(String context) {
        if (hasText(context)) {
            return " [" + context + "]";
        }
        return "";
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}