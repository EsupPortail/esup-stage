package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.CentreGestion;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

@Repository
public class CentreGestionRepository extends PaginationRepository<CentreGestion> {

    public CentreGestionRepository(EntityManager em) {
        super(em, CentreGestion.class, "cg");
        this.predicateWhitelist = Arrays.asList("id", "nomCentre", "niveauCentre.libelle");
    }

    public boolean etablissementExists() {
        String queryString = "SELECT cg.id FROM CentreGestion cg WHERE cg.niveauCentre.libelle = 'ETABLISSEMENT'";
        TypedQuery<Integer> query = em.createQuery(queryString, Integer.class);
        List<Integer> results = query.getResultList();
        return results.size() > 0;
    }

    @Override
    protected void formatFilters(String jsonString) {
        super.formatFilters(jsonString);
        if (filters.has("personnel")) {
            addJoins("JOIN cg.personnels personnel");
        }
    }

    @Override
    protected void addSpecificParameter(String key, JSONObject parameter, List<String> clauses) {
        if (key.equals("personnel")) {
            clauses.add("personnel.uidPersonnel = :" + key.replace(".", ""));
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JSONObject parameter, Query query) {
        if (key.equals("personnel")) {
            query.setParameter(key.replace(".", ""), parameter.getString("value"));
        }
    }
}
