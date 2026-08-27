package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import com.fasterxml.jackson.databind.JsonNode;
import org.esup_portail.esup_stage.model.LangueConvention;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class LangueConventionRepository extends PaginationRepository<LangueConvention> {

    public LangueConventionRepository(EntityManager em) {
        super(em, LangueConvention.class, "lc", "LEFT JOIN lc.templates template");
        this.predicateWhitelist = Arrays.asList("code", "libelle");
        this.specificFilterWhitelist = Arrays.asList("typeConventionTemplate");
    }

    public boolean exists(LangueConvention langueConvention) {
        String queryString = "SELECT code FROM LangueConvention WHERE code = :code";
        TypedQuery<String> query = em.createQuery(queryString, String.class);
        query.setParameter("code", langueConvention.getCode());
        List<String> results = query.getResultList();
        return !results.isEmpty();
    }

    @Override
    protected void addSpecificParameter(String key, JsonNode parameter, List<String> clauses) {
        if (key.equals("typeConventionTemplate")) {
            clauses.add("template.typeConvention.id = :" + key.replace(".", ""));
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JsonNode parameter, Query query) {
        if (key.equals("typeConventionTemplate")) {
            query.setParameter(key.replace(".", ""), getJsonIntValue(parameter));
        }
    }
}
