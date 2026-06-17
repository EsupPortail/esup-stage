package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import com.fasterxml.jackson.databind.JsonNode;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class CentreGestionRepository extends PaginationRepository<CentreGestion> {

    public CentreGestionRepository(EntityManager em) {
        super(em, CentreGestion.class, "cg");
        this.predicateWhitelist = Arrays.asList("id", "nomCentre", "niveauCentre.libelle");
        this.specificFilterWhitelist = Arrays.asList("personnel");
    }

    public boolean etablissementExists() {
        String queryString = "SELECT cg.id FROM CentreGestion cg WHERE cg.niveauCentre.libelle = 'ETABLISSEMENT'";
        TypedQuery<Integer> query = em.createQuery(queryString, Integer.class);
        List<Integer> results = query.getResultList();
        return results.isEmpty();
    }

    @Override
    protected void formatFilters(String jsonString) {
        super.formatFilters(jsonString);
        if (filters.has("personnel")) {
            addJoins("JOIN cg.personnels personnel");
        }
    }

    @Override
    protected void addSpecificParameter(String key, JsonNode parameter, List<String> clauses) {
        if (key.equals("personnel")) {
            clauses.add("personnel.uidPersonnel = :" + key.replace(".", ""));
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JsonNode parameter, Query query) {
        if (key.equals("personnel")) {
            query.setParameter(key.replace(".", ""), getJsonTextValue(parameter));
        }
    }
}
