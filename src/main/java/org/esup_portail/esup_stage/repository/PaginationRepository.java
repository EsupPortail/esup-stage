package org.esup_portail.esup_stage.repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Exportable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import com.google.gson.Gson;

public class PaginationRepository<T extends Exportable> {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurRepository.class);

    protected final EntityManager em;
    protected final Class<T> typeClass;
    protected JSONObject filters;
    protected final String alias;
    protected List<String> predicateWhitelist = new ArrayList<>(); // whitelist pour éviter l'injection sql au niveau du order by
    protected List<String> joins = new ArrayList<>();
    protected JsonObject headers;
    protected String fixJoins;

    public PaginationRepository(EntityManager em, Class<T> typeClass, String alias) {
        this.em = em;
        this.typeClass = typeClass;
        this.alias = alias;
    }

    public PaginationRepository(EntityManager em, Class<T> typeClass, String alias, String fixJoins) {
        this(em, typeClass, alias);
        this.fixJoins = fixJoins;
    }

    public Long count(String filters) {
        formatFilters(filters);
        String queryString = "SELECT COUNT(DISTINCT " + alias + ") FROM " + this.typeClass.getName() + " " + alias + (fixJoins != null ? " " + fixJoins : "");
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
        String queryString = "SELECT DISTINCT " + alias + " FROM " + this.typeClass.getName() + " " + alias + (fixJoins != null ? " " + fixJoins : "");
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
        joins = new ArrayList<>();
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
                addSpecificParameter(key, condition, clauses);
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
                setSpecificParameterValue(key, condition, query);
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

    protected void addSpecificParameter(String key, JSONObject parameter, List<String> clauses) {}
    protected void setSpecificParameterValue(String key, JSONObject parameter, Query query) {}

    protected void formatHeaders(String headerString) {
        headers = new Gson().fromJson(headerString, JsonObject.class);
    }

    public byte[] exportExcel(String headerString, String predicate, String sortOrder, String filters) {
        List<T> data = findPaginated(1, 0, predicate, sortOrder, filters);
        Workbook wb = new HSSFWorkbook();

        formatHeaders(headerString);

        if (headers.has("multipleExcelSheets")){
            JsonArray sheets = headers.getAsJsonArray("multipleExcelSheets");
            for(int i = 0; i < sheets.size(); i++)
            {
                JsonObject object = sheets.get(i).getAsJsonObject();
                String title = object.get("title").getAsString();
                JsonObject columns = object.get("columns").getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entrySet = columns.entrySet();

                createSheet(wb,data,title,entrySet);
            }
        }else{
            String title = "Export";
            JsonObject columns = headers;
            Set<Map.Entry<String, JsonElement>> entrySet = columns.entrySet();
            createSheet(wb,data,title,entrySet);
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            wb.write(bos);
            bos.close();
            return bos.toByteArray();
        } catch (IOException e) {
            logger.error("Erreur génération fichier excel", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la génération du fichier excel");
        }
    }

    public void createSheet(Workbook wb,List<T> data, String title, Set<Map.Entry<String, JsonElement>> entrySet) {

        Sheet sheet = wb.createSheet(title);
        int rowNum = 0;
        int columnNum = 0;

        // Ajout du header
        Row row = sheet.createRow(rowNum);
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            JsonObject header = entry.getValue().getAsJsonObject();
            row.createCell(columnNum).setCellValue(header.get("title").getAsString());
            columnNum++;
        }

        // Ajout des lignes
        rowNum++;
        for (T entity : data) {
            columnNum = 0;
            row = sheet.createRow(rowNum);

            for (Map.Entry<String, JsonElement> entry : entrySet) {
                Cell cell = row.createCell(columnNum);
                cell.setCellValue(entity.getExportValue(entry.getKey()));
                columnNum++;
            }
            rowNum++;
        }
    }

    public StringBuilder exportCsv(String headerString, String predicate, String sortOrder, String filters) {
        List<T> data = findPaginated(1, 0, predicate, sortOrder, filters);
        String newLine = System.lineSeparator();
        formatHeaders(headerString);

        JsonObject columns = null;
        if (headers.has("multipleExcelSheets")){
            JsonArray sheets = headers.getAsJsonArray("multipleExcelSheets");
            for(int i = 0; i < sheets.size(); i++)
            {
                JsonObject object = sheets.get(i).getAsJsonObject();
                if(columns == null)
                    columns = object.get("columns").getAsJsonObject();
                else{
                    JsonObject newColumns = object.get("columns").getAsJsonObject();
                    for(String key : newColumns.keySet())
                    {
                        if(!columns.has(key))
                            columns.add(key,newColumns.get(key));
                    }
                }
            }
        }else{
            columns = headers;
        }

        Set<Map.Entry<String, JsonElement>> entrySet = columns.entrySet();

        StringBuilder sb = new StringBuilder();

        // Ajout du header
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            JsonObject header = entry.getValue().getAsJsonObject();
            sb.append("\"").append(header.get("title").getAsString()).append("\"").append(";");
        }
        sb.append(newLine);

        for (T entity : data) {
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                String value = entity.getExportValue(entry.getKey());
                if (value != null) {
                    value = value.replaceAll("\n", "").replaceAll("\r", "");
                }
                sb.append("\"").append(value).append("\"").append(";");
            }
            sb.append(newLine);
        }

        return sb;
    }
}
