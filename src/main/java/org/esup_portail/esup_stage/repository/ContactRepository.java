package org.esup_portail.esup_stage.repository;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.esup_portail.esup_stage.model.Contact;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ContactRepository extends PaginationRepository<Contact> {
    private static final String VISIBLE_FOR_CENTRES = "visibleForCentres";

    public ContactRepository(EntityManager em) {
        super(em, Contact.class, "c");
        this.specificFilterWhitelist.add(VISIBLE_FOR_CENTRES);
    }

    public Long countVisibleForCentres(List<Integer> centreIds, String filters) {
        return super.count(withVisibilityFilter(filters, centreIds));
    }

    public List<Contact> findPaginatedVisibleForCentres(List<Integer> centreIds, int page, int perPage, String predicate, String sortOrder, String filters) {
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
            clauses.add("c.centreGestion.id IN :visibleForCentres");
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
