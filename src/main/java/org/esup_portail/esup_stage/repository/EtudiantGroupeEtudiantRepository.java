package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.EtudiantGroupeEtudiant;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EtudiantGroupeEtudiantRepository extends PaginationRepository<EtudiantGroupeEtudiant> {

    public EtudiantGroupeEtudiantRepository(EntityManager em) {
        super(em, EtudiantGroupeEtudiant.class, "ege");
    }


    @Override
    protected void addSpecificParameter(String key, JSONObject parameter, List<String> clauses) {
        if (key.equals("etape.id")) {
            JSONArray jsonArray = parameter.getJSONArray("value");
            List<String> clauseOr = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                clauseOr.add("(ege.convention.etape.id.code = :codeEtape" + i + " AND ege.convention.etape.id.codeUniversite = :codeUnivEtape" + i + " AND ege.convention.etape.id.codeVersionEtape = :versionEtape" + i + ")");
            }
            if (clauseOr.size() > 0) {
                clauses.add("(" + String.join(" OR ", clauseOr) + ")");
            }
        }
        if (key.equals("ufr.id")) {
            JSONArray jsonArray = parameter.getJSONArray("value");
            List<String> clauseOr = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                clauseOr.add("(ege.convention.ufr.id.code = :codeUfr" + i + " AND ege.convention.ufr.id.codeUniversite = :codeUnivUfr" + i + ")");
            }
            clauses.add("(" + String.join(" OR ", clauseOr) + ")");
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JSONObject parameter, Query query) {
        if (key.equals("etape.id")) {
            JSONArray jsonArray = parameter.getJSONArray("value");
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonEtapeId = jsonArray.getJSONObject(i);
                query.setParameter("codeEtape" + i, jsonEtapeId.getString("code"));
                query.setParameter("codeUnivEtape" + i, jsonEtapeId.getString("codeUniversite"));
                query.setParameter("versionEtape" + i, jsonEtapeId.getString("codeVersionEtape"));
            }
        }
        if (key.equals("ufr.id")) {
            JSONArray jsonArray = parameter.getJSONArray("value");
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonUfrId = jsonArray.getJSONObject(i);
                query.setParameter("codeUfr" + i, jsonUfrId.getString("code"));
                query.setParameter("codeUnivUfr" + i, jsonUfrId.getString("codeUniversite"));
            }
        }
    }
}