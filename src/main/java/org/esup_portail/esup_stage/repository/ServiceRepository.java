package org.esup_portail.esup_stage.repository;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.esup_portail.esup_stage.model.Service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class ServiceRepository extends PaginationRepository<Service> {
    private static final String VISIBLE_FOR_CENTRES = "visibleForCentres";
    private static final String INUTILISE = "inutilise";

    // Service « inutilisé » : plus aucun contact et non référencé par une convention ou un avenant
    // (identique au critère du nettoyage automatique, voir ServiceJpaRepository.findInutilisesPourNettoyage)
    private static final String CRITERE_INUTILISE =
            "NOT EXISTS (SELECT 1 FROM Contact c WHERE c.service = s)" +
            " AND NOT EXISTS (SELECT 1 FROM Convention cv WHERE cv.service = s)" +
            " AND NOT EXISTS (SELECT 1 FROM Avenant a WHERE a.service = s)";

    public ServiceRepository(EntityManager em) {
        super(em, Service.class, "s");
        this.predicateWhitelist = Arrays.asList("nom", "pays.lib", "commune", "voie", "structure.raisonSociale");
        this.specificFilterWhitelist.add(VISIBLE_FOR_CENTRES);
        this.specificFilterWhitelist.add(INUTILISE);
    }

    public Long countVisibleForCentres(List<Integer> centreIds, String filters) {
        return super.count(withVisibilityFilter(filters, centreIds));
    }

    public List<Service> findPaginatedVisibleForCentres(List<Integer> centreIds, int page, int perPage, String predicate, String sortOrder, String filters) {
        return super.findPaginated(page, perPage, predicate, sortOrder, withVisibilityFilter(filters, centreIds));
    }

    @Override
    protected void addSpecificParameter(String key, JsonNode parameter, List<String> clauses) {
        if (VISIBLE_FOR_CENTRES.equals(key)) {
            JsonNode centreIds = parameter.get("value");
            if (centreIds.isEmpty()) {
                clauses.add("1 = 0");
                return;
            }
            clauses.add("(s.centreGestion.id IN :visibleForCentres OR s.centreGestion.codeConfidentialite IS NULL OR s.centreGestion.codeConfidentialite.code = '0' OR (s.centreGestion.codeConfidentialite.code = '2' AND s.centreGestion.codeConfidentialiteConventionOrpheline.code = '0'))");
        }
        if (INUTILISE.equals(key)) {
            clauses.add("(" + CRITERE_INUTILISE + ")");
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JsonNode parameter, Query query) {
        if (VISIBLE_FOR_CENTRES.equals(key)) {
            JsonNode jsonArray = parameter.get("value");
            List<Integer> values = new ArrayList<>();
            for (JsonNode item : jsonArray) {
                values.add(item.asInt());
            }
            if (!values.isEmpty()) {
                query.setParameter(VISIBLE_FOR_CENTRES, values);
            }
        }
    }

    private String withVisibilityFilter(String filters, List<Integer> centreIds) {
        JSONObject jsonFilters = new JSONObject(filters);
        JSONObject visibilityFilter = new JSONObject();
        visibilityFilter.put("specific", true);
        visibilityFilter.put("value", centreIds == null ? new JSONArray() : new JSONArray(centreIds));
        jsonFilters.put(VISIBLE_FOR_CENTRES, visibilityFilter);
        return jsonFilters.toString();
    }
}
