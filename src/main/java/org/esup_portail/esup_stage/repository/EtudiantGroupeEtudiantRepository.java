package org.esup_portail.esup_stage.repository;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.esup_portail.esup_stage.model.EtudiantGroupeEtudiant;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class EtudiantGroupeEtudiantRepository extends PaginationRepository<EtudiantGroupeEtudiant> {

    public EtudiantGroupeEtudiantRepository(EntityManager em) {
        super(em, EtudiantGroupeEtudiant.class, "ege");
        this.specificFilterWhitelist = Arrays.asList("etape.id", "ufr.id");
    }


    @Override
    protected void addSpecificParameter(String key, JsonNode parameter, List<String> clauses) {
        if (key.equals("etape.id")) {
            JsonNode jsonArray = parameter.get("value");
            List<String> clauseOr = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); ++i) {
                clauseOr.add("(ege.convention.etape.id.code = :codeEtape" + i + " AND ege.convention.etape.id.codeUniversite = :codeUnivEtape" + i + " AND ege.convention.etape.id.codeVersionEtape = :versionEtape" + i + ")");
            }
            if (clauseOr.isEmpty()) {
                clauses.add("(" + String.join(" OR ", clauseOr) + ")");
            }
        }
        if (key.equals("ufr.id")) {
            JsonNode jsonArray = parameter.get("value");
            List<String> clauseOr = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); ++i) {
                clauseOr.add("(ege.convention.ufr.id.code = :codeUfr" + i + " AND ege.convention.ufr.id.codeUniversite = :codeUnivUfr" + i + ")");
            }
            clauses.add("(" + String.join(" OR ", clauseOr) + ")");
        }
        if (key.equals("convention.structure.id")) {
            clauses.add("(" +
                    "ege.convention.structure.id IN :structureId" +
                    " OR (ege.convention.structure.id IS NULL" +
                        " AND ege.groupeEtudiant.convention.structure.id IN :structureId)" +
                ")");
        }
        if (key.equals("convention.service.id")) {
            clauses.add("(" +
                    "ege.convention.service.id IN :serviceId" +
                    " OR (ege.convention.service.id IS NULL" +
                    " AND ege.groupeEtudiant.convention.service.id IN :serviceId)" +
                    ")");
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JsonNode parameter, Query query) {
        if (key.equals("etape.id")) {
            JsonNode jsonArray = parameter.get("value");
            for (int i = 0; i < jsonArray.size(); ++i) {
                JsonNode jsonEtapeId = jsonArray.get(i);
                query.setParameter("codeEtape" + i, jsonEtapeId.get("code").asText());
                query.setParameter("codeUnivEtape" + i, jsonEtapeId.get("codeUniversite").asText());
                query.setParameter("versionEtape" + i, jsonEtapeId.get("codeVersionEtape").asText());
            }
        }
        if (key.equals("ufr.id")) {
            JsonNode jsonArray = parameter.get("value");
            for (int i = 0; i < jsonArray.size(); ++i) {
                JsonNode jsonUfrId = jsonArray.get(i);
                query.setParameter("codeUfr" + i, jsonUfrId.get("code").asText());
                query.setParameter("codeUnivUfr" + i, jsonUfrId.get("codeUniversite").asText());
            }
        }
        if (key.equals("convention.structure.id")) {
            query.setParameter("structureId", parameter.getJSONArray("value").toList());
        }
        if (key.equals("convention.service.id")) {
            query.setParameter("serviceId", parameter.getJSONArray("value").toList());
        }
    }
}
