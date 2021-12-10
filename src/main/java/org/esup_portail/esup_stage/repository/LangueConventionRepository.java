package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.LangueConvention;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

@Repository
public class LangueConventionRepository extends PaginationRepository<LangueConvention> {

    public LangueConventionRepository(EntityManager em) {
        super(em, LangueConvention.class, "lc");
        this.predicateWhitelist = Arrays.asList("code", "libelle");
    }

    public boolean exists(LangueConvention langueConvention) {
        String queryString = "SELECT code FROM LangueConvention WHERE code = :code";
        TypedQuery<String> query = em.createQuery(queryString, String.class);
        query.setParameter("code", langueConvention.getCode());
        List<String> results = query.getResultList();
        return results.size() > 0;
    }

    @Override
    protected void formatFilters(String jsonString) {
        super.formatFilters(jsonString);
        if (filters.has("typeConventionTemplate")) {
            addJoins("JOIN lc.templates template");
        }
    }

    @Override
    protected void addSpecificParameter(String key, JSONObject parameter, List<String> clauses) {
        if (key.equals("typeConventionTemplate")) {
            clauses.add("template.typeConvention.id = :" + key.replace(".", ""));
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JSONObject parameter, Query query) {
        if (key.equals("typeConventionTemplate")) {
            query.setParameter(key.replace(".", ""), parameter.getInt("value"));
        }
    }
}
