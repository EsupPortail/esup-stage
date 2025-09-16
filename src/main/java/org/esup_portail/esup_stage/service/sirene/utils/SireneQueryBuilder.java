package org.esup_portail.esup_stage.service.sirene.utils;

import org.esup_portail.esup_stage.repository.NafN5JpaRepository;
import org.esup_portail.esup_stage.repository.StatutJuridiqueJpaRepository;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SireneQueryBuilder {

    private static StatutJuridiqueJpaRepository statutJuridiqueJpaRepository;

    private static NafN5JpaRepository nafN5JpaRepository;

    public SireneQueryBuilder(@Autowired StatutJuridiqueJpaRepository statutJuridiqueJpaRepository, @Autowired NafN5JpaRepository nafN5JpaRepository) {
        SireneQueryBuilder.statutJuridiqueJpaRepository = statutJuridiqueJpaRepository;
        SireneQueryBuilder.nafN5JpaRepository = nafN5JpaRepository;
    }

    //    TODO enlever les société fermées
//    Société (unité légale)
//        → uniteLegale.etatAdministratifUniteLegale
//            A = Active
//            C = Cessée (donc société fermée)

    public static String buildLuceneQuery(String filtersJson) {
        JSONObject filters = new JSONObject(filtersJson);
        List<String> clauses = new ArrayList<>();

        for (String key : filters.keySet()) {
            if ("pays.id".equals(key)) continue;

            JSONObject cond = filters.getJSONObject(key);
            if (!cond.has("value")) continue;

            String raw = cond.get("value").toString().trim();
            if (raw.isEmpty()) continue;
            String field = mapToApiField(key);

            if ("raisonSociale".equals(key)) {
                String cleaned = raw.replaceAll("\"", "").trim();
                String clause = field + ":\"" + cleaned + "*\"";
                clauses.add("(" + clause + ")");
                continue;
            }

            if ("statutJuridique.id".equals(key)) {
                JSONArray arr = cond.getJSONArray("value");
                List<Integer> ids = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    ids.add(arr.getInt(i));
                }
                // Récupère les codes
                List<String> codes = statutJuridiqueJpaRepository.findCodeByIdIn(ids);
                if (codes == null || codes.isEmpty()) {
                    continue; // on retire le filtre de la requête
                }
                List<String> ors = new ArrayList<>();
                for (String code : codes) {
                    if (code != null && !code.trim().isEmpty()) {
                        ors.add(field + ":" + formatFieldValue(field, code));
                    }
                }
                if (ors.isEmpty()) continue; // Si tous les codes sont vides, pareil
                String clause = "(" + String.join(" OR ", ors) + ")";
                clauses.add(clause);
                continue;
            }

            if ("nafN1.code".equals(key)) {
                JSONArray arr = cond.getJSONArray("value");
                List<String> nafN1Codes = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    nafN1Codes.add(arr.getString(i));
                }
                // Va chercher tous les NAF N5 pour ces N1
                List<String> nafN5Codes = nafN5JpaRepository.findAllCodesByNafN1Codes(nafN1Codes);
                if (nafN5Codes == null || nafN5Codes.isEmpty()) continue;
                List<String> ors = new ArrayList<>();
                for (String naf5 : nafN5Codes) {
                    ors.add(field + ":" + formatFieldValue(field, naf5));
                }
                if (ors.isEmpty()) continue;
                String clause = "(" + String.join(" OR ", ors) + ")";
                clauses.add(clause);


                continue;
            }

            if ("typeStructure.id".equals(key)) {
                // Peut être String, Integer, ou Array
                Object value = cond.get("value");
                List<Integer> ids = new ArrayList<>();
                if (value instanceof JSONArray arr) {
                    for (int i = 0; i < arr.length(); i++) {
                        ids.add(arr.getInt(i));
                    }
                } else if (value instanceof Number n) {
                    ids.add(n.intValue());
                } else {
                    // Si c'est une string "3", "2", etc.
                    try {
                        ids.add(Integer.parseInt(value.toString()));
                    } catch (Exception e) {
                        continue; // skip si non parseable
                    }
                }

                List<String> ors = new ArrayList<>();
                for (Integer id : ids) {
                    List<String> codes = TYPE_STRUCTURE_TO_CODES.get(id);
                    if (codes != null) {
                        ors.addAll(codes.stream().map(code -> "categorieJuridiqueUniteLegale:" + code).toList());
                    }
                }
                if (!ors.isEmpty()) {
                    String clause = "(" + String.join(" OR ", ors) + ")";
                    clauses.add(clause);
                }
                continue;
            }

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
                        Object value = arr.get(i);
                        String v = value.toString().trim();
                        if (!v.isEmpty()) {
                            if (NO_WILDCARD_FIELDS.contains(field)) {
                                // Pour les champs sans wildcard, on utilise la valeur exacte
                                ors.add(field + ":" + formatFieldValue(field, v));
                            } else {
                                // Pour les champs avec wildcard
                                ors.add(field + ":\"" + v + "*\"");
                            }
                        }
                    }
                    clause = "(" + String.join(" OR ", ors) + ")";
                    break;
                default:
                    if (NO_WILDCARD_FIELDS.contains(field)) {
                        // Pour les champs sans wildcard, on utilise la valeur exacte
                        clause = field + ":" + formatFieldValue(field, raw);
                    } else {
                        // Pour les champs avec wildcard
                        clause = field + ":" + raw + "*";
                    }
            }

            clauses.add("(" + clause + ")");
        }

        String query = String.join(" AND ", clauses);
        return query.isEmpty() ? "" : query;
    }

    /**
     * Formate la valeur selon le type de champ
     */
    private static String formatFieldValue(String field, String value) {
        switch (field) {
            case "categorieJuridiqueUniteLegale":
                // Les catégories juridiques sont des codes numériques (ex: 1000, 5710)
                // On s'assure que c'est un nombre et on le retourne tel quel
                try {
                    Integer.parseInt(value);
                    return value;
                } catch (NumberFormatException e) {
                    // Si ce n'est pas un nombre, on le retourne entre guillemets
                    return "\"" + value + "\"";
                }

            case "activitePrincipaleUniteLegale":
                // Les codes NAF peuvent contenir des points (ex: 85.59B)
                // On les retourne entre guillemets pour éviter les problèmes de parsing
                return "\"" + value + "\"";

            case "siret":
            case "siren":
            case "nic":
                // Les identifiants numériques sans guillemets
                return value;

            default:
                // Par défaut, on met entre guillemets pour éviter les problèmes
                return "\"" + value + "\"";
        }
    }

    private static String mapToApiField(String key) {
        return switch (key) {
            case "raisonSociale" -> "denominationUniteLegale";
            case "numeroSiret" -> "siret";
            case "commune" -> "libelleCommuneEtablissement";
            case "nafN1.code" -> "activitePrincipaleUniteLegale";
            case "statutJuridique.id" -> "categorieJuridiqueUniteLegale";
            case "typeStructure.id" -> "categorieJuridiqueUniteLegale";
            default -> key.replaceAll("([a-z])([A-Z])", "$1_$2")
                    .replace(".", "_")
                    .toLowerCase();
        };
    }

    /**
     * Champs qui ne doivent pas avoir de wildcards
     * Ces champs nécessitent des valeurs exactes
     */
    private static final Set<String> NO_WILDCARD_FIELDS = Set.of(
            "siret",
            "siren",
            "nic",
            "categorieJuridiqueUniteLegale",
            "activitePrincipaleUniteLegale"
    );

    public static final Map<Integer, List<String>> TYPE_STRUCTURE_TO_CODES = Map.of(
            // ADMINISTRATION
            1, List.of(
                    "7*", // Tous les codes commençant par 7
                    "4*", // Tous les codes commençant par 4
                    "8*", // Tous les codes commençant par 8
                    "32*", // Tous les codes commençant par 32
                    "71*", "72*", "73*", "74*"
            ),
            // ASSOCIATION
            2, List.of(
                    "9*",
                    "92*", "93*", "62*", "63*"
            ),
            // ENTREPRISE PRIVEE
            3, List.of(
                    "2*", "3*", "5*", "6*", "31*"
            ),
            // ENTREPRISE PUBLIQUE / SEM
            4, List.of(
                    "41*", "55*", "56*", "57*",
                    "5515", "5615"
            ),
            // MUTUELLE COOPERATIVE
            5, List.of(
                    "51*", "65*", "81*", "82*", "83*", "84*", "85*"
            ),
            // ETABLISSEMENT ENSEIGNEMENT
            6, List.of(
                    "7331", "7383", "7384"
            )
    );
}