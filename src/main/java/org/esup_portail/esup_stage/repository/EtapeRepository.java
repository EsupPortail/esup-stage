package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import com.fasterxml.jackson.databind.JsonNode;
import org.esup_portail.esup_stage.model.Etape;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class EtapeRepository extends PaginationRepository<Etape> {

    public EtapeRepository(EntityManager em) {
        super(em, Etape.class, "e");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
        this.specificFilterWhitelist = Arrays.asList("search");
    }

    @Override
    protected void addSpecificParameter(String key, JsonNode parameter, List<String> clauses) {
        if (key.equals("search")) {
            clauses.add("(e.libelle LIKE :search OR CONCAT(e.id.code, '-', e.id.codeVersionEtape) LIKE :search)");
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JsonNode parameter, Query query) {
        if (key.equals("search")) {
            query.setParameter(key.replace(".", ""), "%" + getJsonTextValue(parameter) + "%");
        }
    }
}
