package org.esup_portail.esup_stage.repository;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PaginationRepository<T> {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurRepository.class);

    protected final EntityManager em;
    protected final Class<T> typeClass;
    protected JSONObject filters;
    protected final String alias;
    protected List<String> predicateWhitelist = new ArrayList<>(); // whitelist pour éviter l'injection sql au niveau du order by
    protected List<String> joins = new ArrayList<>();

    public PaginationRepository(EntityManager em, Class<T> typeClass, String alias) {
        this.em = em;
        this.typeClass = typeClass;
        this.alias = alias;
    }

    public Long count(String filters) {
        formatFilters(filters);
        String queryString = "SELECT COUNT(DISTINCT " + alias + ") FROM " + this.typeClass.getName() + " " + alias;
        List<String> clauses = getClauses();
        queryString += " " + String.join(" ", joins);
        if (clauses.size() > 0) {
            queryString += " WHERE " + String.join(" AND ", clauses);
        }

        TypedQuery<Long> query = em.createQuery(queryString, Long.class);
        setParameters(query);

        logger.debug("Dynamic query string: " + queryString);
        Set<Parameter<?>> parameters = query.getParameters();
        logger.debug("Parameters: {" + parameters.stream().map(p -> p.getName() + ": " + query.getParameterValue(p.getName())).collect(Collectors.joining(", ")) + "}");

        return query.getSingleResult();
    }

    public List<T> findPaginated(int page, int perPage, String predicate, String sortOrder, String filters) {
        formatFilters(filters);
        String queryString = "SELECT DISTINCT " + alias + " FROM " + this.typeClass.getName() + " " + alias;
        List<String> clauses = getClauses();
        queryString += " " + String.join(" ", joins);
        if (clauses.size() > 0) {
            queryString += " WHERE " + String.join(" AND ", clauses);
        }
        if (predicate != null && predicateWhitelist.contains(predicate)) {
            queryString += " ORDER BY " + alias + "." + predicate;
            if (sortOrder.equalsIgnoreCase("asc")) {
                queryString += " ASC";
            } else {
                queryString += " DESC";
            }
        }

        TypedQuery<T> query = em.createQuery(queryString, this.typeClass);
        setParameters(query);

        if (page > 0) {
            query.setFirstResult((page-1) * perPage);
        }
        if (perPage > 0) {
            query.setMaxResults(perPage);
        }

        logger.debug("Dynamic query string: " + queryString);
        Set<Parameter<?>> parameters = query.getParameters();
        logger.debug("Parameters: {" + parameters.stream().map(p -> p.getName() + ": " + query.getParameterValue(p.getName())).collect(Collectors.joining(", ")) + "}");

        return query.getResultList();
    }

    protected void formatFilters(String jsonString) {
        filters = new JSONObject(jsonString);
    }

    protected void addJoins(String join) {
        if (!joins.contains(join)) {
            joins.add(join);
        }
    }

    private List<String> getClauses() {
        List<String> clauses = new ArrayList<>();
        for (int i = 0; i < filters.length(); ++ i) {
            String key = filters.names().getString(i);
            String column = alias + "." + key;
            JSONObject condition = filters.getJSONObject(key);
            if (condition.has("specific") && condition.getBoolean("specific")) {
                addSpecificParemeter(key, condition, clauses);
            } else {
                String op;
                switch (condition.getString("type")) {
                    case "int":
                    case "boolean":
                    case "date":
                        op = "=";
                        break;
                    case "date-min":
                        op = ">=";
                        break;
                    case "date-max":
                        op = "<=";
                        break;
                    case "list":
                        op = "IN";
                        break;
                    default:
                        column = "LOWER(" + column + ")";
                        op = "LIKE";
                        break;
                }
                String value = ":" + key.replace(".", "");
                clauses.add(column + " " + op + " " + value);
            }
        }
        return clauses;
    }

    private void setParameters(Query query) {
        for (int i = 0; i < filters.length(); ++ i) {
            String key = filters.names().getString(i);
            JSONObject condition = filters.getJSONObject(key);
            if (condition.has("specific") && condition.getBoolean("specific")) {
                setSpecificParemeterValue(key, condition, query);
            } else {
                switch (condition.getString("type")) {
                    case "int":
                        query.setParameter(key.replace(".", ""), condition.getInt("value"));
                        break;
                    case "boolean":
                        query.setParameter(key.replace(".", ""), condition.getBoolean("value"));
                        break;
                    case "date":
                    case "date-min":
                    case "date-max":
                        Date date = new Date(condition.getLong("value"));
                        query.setParameter(key.replace(".", ""), date);
                        break;
                    case "list":
                        List<Object> values = new ArrayList<>();
                        JSONArray jsonArray = condition.getJSONArray("value");
                        for (int j = 0 ; j < jsonArray.length(); ++j) {
                            values.add(jsonArray.get(j));
                        }
                        query.setParameter(key.replace(".", ""), values);
                        break;
                    default:
                        query.setParameter(key.replace(".", ""), "%" + condition.getString("value").toLowerCase() + "%");
                        break;
                }
            }
        }
    }

    public boolean exists(String libelle, int id) {
        String queryString = "SELECT id FROM " + this.typeClass.getName() + " WHERE libelle = :libelle";
        TypedQuery<Integer> query = em.createQuery(queryString, Integer.class);
        query.setParameter("libelle", libelle);
        List<Integer> results = query.getResultList();
        if (id == 0 && results.size() > 0) {
            return true;
        }

        return results.stream().anyMatch(i -> i != id);
    }

    protected void addSpecificParemeter(String key, JSONObject parameter, List<String> clauses) {}
    protected void setSpecificParemeterValue(String key, JSONObject parameter, Query query) {}
}
