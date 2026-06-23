package org.esup_portail.esup_stage.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Structure;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Repository
public class StructureRepository extends PaginationRepository<Structure> {
    public StructureRepository(EntityManager em) {
        super(em, Structure.class, "s");
        this.predicateWhitelist = Arrays.asList("raisonSociale", "numeroSiret", "numeroUAI", "nafN5.nafN1.libelle", "pays.lib", "commune", "typeStructure.libelle", "statutJuridique.libelle");
        this.specificFilterWhitelist = Arrays.asList("nafN1.code");
    }

    @Override
    public List<Structure> findPaginated(int page, int perPage, String predicate, String sortOrder, String filters) {
        ObjectNode jsonFilters;
        try {
            JsonNode parsedFilters = JSON_MAPPER.readTree(filters == null || filters.isBlank() ? "{}" : filters);
            if (!parsedFilters.isObject()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Filtres invalides");
            }
            jsonFilters = (ObjectNode) parsedFilters;
        } catch (JsonProcessingException e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Filtres invalides");
        }

        if (!jsonFilters.has("temEnServStructure")) {
            ObjectNode temEnServParam = newJsonObjectNode();
            temEnServParam.put("value", true);
            temEnServParam.put("type", "boolean");
            jsonFilters.set("temEnServStructure", temEnServParam);

            filters = jsonFilters.toString();
        }

        return super.findPaginated(page, perPage, predicate, sortOrder, filters);
    }



    @Override
    protected void addSpecificParameter(String key, JsonNode parameter, List<String> clauses) {
        if (key.equals("nafN1.code")) {
            clauses.add("s.nafN5.nafN1.code IN :" + key.replace(".", ""));
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JsonNode parameter, Query query) {
        if (key.equals("nafN1.code")) {
            query.setParameter(key.replace(".", ""), getJsonArrayValues(parameter));
        }
    }

    public boolean existsSiret(Structure structure, String siret) {
        String queryString = "SELECT id FROM Structure WHERE LOWER(numeroSiret) = LOWER(:siret) AND temEnServStructure = true";
        TypedQuery<Integer> query = em.createQuery(queryString, Integer.class);
        query.setParameter("siret", siret);
        List<Integer> results = query.getResultList();
        if (structure.getId() == 0 && results.isEmpty()) {
            return true;
        }

        return results.stream().anyMatch(i -> !Objects.equals(i, structure.getId()));
    }
}
