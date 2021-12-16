package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Pays;
import org.esup_portail.esup_stage.model.Structure;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class StructureRepository extends PaginationRepository<Structure> {
    public StructureRepository(EntityManager em) {
        super(em, Structure.class, "s");
        this.predicateWhitelist = Arrays.asList("raisonSociale", "numeroSiret", "nafN5.nafN1.libelle", "pays.lib", "commune", "typeStructure.libelle", "statutJuridique.libelle");
    }

    @Override
    protected void addSpecificParameter(String key, JSONObject parameter, List<String> clauses) {
        if (key.equals("nafN1.code")) {
            clauses.add("s.nafN5.nafN1.code IN :" + key.replace(".", ""));
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JSONObject parameter, Query query) {
        if (key.equals("nafN1.code")) {
            List<Object> values = new ArrayList<>();
            JSONArray jsonArray = parameter.getJSONArray("value");
            for (int j = 0 ; j < jsonArray.length(); ++j) {
                values.add(jsonArray.get(j));
            }
            query.setParameter(key.replace(".", ""), values);
        }
    }

    public boolean existsSiret(Structure structure, String siret) {
        String queryString = "SELECT id FROM Structure WHERE LOWER(numeroSiret) = LOWER(:siret)";
        TypedQuery<Integer> query = em.createQuery(queryString, Integer.class);
        query.setParameter("siret", siret);
        List<Integer> results = query.getResultList();
        if (structure.getId() == 0 && results.size() > 0) {
            return true;
        }

        return results.stream().anyMatch(i -> i != structure.getId());
    }
}
