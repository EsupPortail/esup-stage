package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Etape;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.List;

@Repository
public class EtapeRepository extends PaginationRepository<Etape> {

    public EtapeRepository(EntityManager em) {
        super(em, Etape.class, "e");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }

    @Override
    protected void addSpecificParameter(String key, JSONObject parameter, List<String> clauses) {
        if (key.equals("search")) {
            clauses.add("(e.libelle LIKE :search OR CONCAT(e.id.code, '-', e.id.codeVersionEtape) LIKE :search)");
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JSONObject parameter, Query query) {
        if (key.equals("search")) {
            query.setParameter(key.replace(".", ""), "%" + parameter.getString("value") + "%");
        }
    }
}
