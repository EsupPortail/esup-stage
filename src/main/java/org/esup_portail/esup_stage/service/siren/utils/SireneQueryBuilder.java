package org.esup_portail.esup_stage.service.siren.utils;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;

public class SireneQueryBuilder {

    public static String buildLuceneQuery(String filtersJson) {
        JSONObject filters = new JSONObject(filtersJson);
        List<String> clauses = new ArrayList<>();

        for (String key : filters.keySet()) {
            JSONObject cond = filters.getJSONObject(key);
            if (!cond.has("value")) continue;

            String raw = cond.get("value").toString().trim();
            if (raw.isEmpty()) continue;
            String field = mapToApiField(key);

            String clause;
            switch (cond.optString("type", "text")) {
                case "int":
                case "number":
                    clause = field + ":" + raw;
                    break;
                case "list":
                    JSONArray arr = cond.getJSONArray("value");
                    List<String> ors = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        String v = arr.getString(i).trim();
                        if (!v.isEmpty()) {
                            if (NO_WILDCARD_FIELDS.contains(field)) {
                                ors.add(field + ":" + v);
                            } else {
                                ors.add(field + ":\"" + v + "*\"");
                            }
                        }
                    }
                    clause = "(" + String.join(" OR ", ors) + ")";
                    break;
                default:
                    if (NO_WILDCARD_FIELDS.contains(field)) {
                        clause = field + ":" + raw;
                    } else {
                        clause = field + ":" + raw + "*";
                    }
            }

            clauses.add("(" + clause + ")");
        }

        String query = String.join(" AND ", clauses);
        return query.isEmpty() ? "" : query;
    }




    private static String mapToApiField(String key) {
        return switch (key) {
            case "raisonSociale"    -> "denominationUniteLegale";
            case "numeroSiret"      -> "siret";
            case "commune"          -> "libelleCommuneEtablissement";
            case "codePostal"       -> "codePostalEtablissement";
            case "pays.id"          -> "codePaysEtablissement";
            default                 -> key.replaceAll("([a-z])([A-Z])", "$1_$2")
                    .replace(".", "_")
                    .toLowerCase();
        };
    }

    private static final Set<String> NO_WILDCARD_FIELDS = Set.of(
            "siret", "siren", "nic"
    );
}
