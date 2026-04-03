package org.esup_portail.esup_stage.repository;

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

    public ServiceRepository(EntityManager em) {
        super(em, Service.class, "s");
        this.predicateWhitelist = Arrays.asList("nom", "pays.lib", "commune", "voie");
    }

    public Long countVisibleForCentres(List<Integer> centreIds, String filters) {
        return super.count(withVisibilityFilter(filters, centreIds));
    }

    public List<Service> findPaginatedVisibleForCentres(List<Integer> centreIds, int page, int perPage, String predicate, String sortOrder, String filters) {
        return super.findPaginated(page, perPage, predicate, sortOrder, withVisibilityFilter(filters, centreIds));
    }

    @Override
    protected void addSpecificParameter(String key, JSONObject parameter, List<String> clauses) {
        if (VISIBLE_FOR_CENTRES.equals(key)) {
            JSONArray centreIds = parameter.getJSONArray("value");
            if (centreIds.isEmpty()) {
                clauses.add("1 = 0");
                return;
            }
            clauses.add("(s.centreGestion.id IN :visibleForCentres OR s.centreGestion.codeConfidentialite IS NULL OR s.centreGestion.codeConfidentialite.code = '0' OR (s.centreGestion.codeConfidentialite.code = '2' AND s.centreGestion.codeConfidentialiteConventionOrpheline.code = '0'))");
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JSONObject parameter, Query query) {
        if (VISIBLE_FOR_CENTRES.equals(key)) {
            JSONArray jsonArray = parameter.getJSONArray("value");
            List<Integer> values = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                values.add(jsonArray.getInt(i));
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