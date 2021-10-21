package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Convention;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.List;

@Repository
public class ConventionRepository extends PaginationRepository<Convention> {

    public ConventionRepository(EntityManager em) {
        super(em, Convention.class, "c");
        this.predicateWhitelist = Arrays.asList("id");
    }

    @Override
    protected void formatFilters(String jsonString) {
        super.formatFilters(jsonString);
        if (filters.has("centreGestion.personnels")) {
            addJoins("JOIN c.centreGestion.personnels personnel");
        }
    }

    @Override
    protected void addSpecificParemeter(String key, JSONObject parameter, List<String> clauses) {
        if (key.equals("centreGestion.personnels")) {
            clauses.add("personnel.uidPersonnel = :" + key.replace(".", ""));
        }
    }

    @Override
    protected void setSpecificParemeterValue(String key, JSONObject parameter, Query query) {
        if (key.equals("centreGestion.personnels")) {
            query.setParameter(key.replace(".", ""), parameter.getString("value"));
        }
    }
}
