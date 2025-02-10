package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Convention;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ConventionRepository extends PaginationRepository<Convention> {

    public ConventionRepository(EntityManager em) {
        super(em, Convention.class, "c");
        this.predicateWhitelist = Arrays.asList("id", "etudiant.prenom", "etudiant.nom", "etudiant.nom_etudiant.prenom", "structure.raisonSociale", "dateDebutStage", "dateFinStage", "ufr.libelle", "etape.libelle", "enseignant.prenom", "sujetStage", "lieuStage", "annee");
    }

    @Override
    protected void formatFilters(String jsonString) {
        super.formatFilters(jsonString);
        if (filters.has("centreGestion.personnels")) {
            addJoins("JOIN c.centreGestion.personnels personnel");
        }
        if (filters.has("avenant")) {
            addJoins("LEFT JOIN c.avenants avenant");
        }
    }

    @Override
    protected void addSpecificParameter(String key, JSONObject parameter, List<String> clauses) {
        if (key.equals("centreGestion.personnels")) {
            clauses.add("personnel.uidPersonnel = :" + key.replace(".", ""));
        }
        if (key.equals("enseignant.uidEnseignant")) {
            clauses.add("c.enseignant.uidEnseignant = :" + key.replace(".", ""));
        }
        if (key.equals("etudiant.identEtudiant")) {
            clauses.add("c.etudiant.identEtudiant = :" + key.replace(".", ""));
        }
        if (key.equals("etape.id")) {
            JSONArray jsonArray = parameter.getJSONArray("value");
            List<String> clauseOr = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                clauseOr.add("(c.etape.id.code = :codeEtape" + i + " AND c.etape.id.codeUniversite = :codeUnivEtape" + i + " AND c.etape.id.codeVersionEtape = :versionEtape" + i + ")");
            }
            if (clauseOr.size() > 0) {
                clauses.add("(" + String.join(" OR ", clauseOr) + ")");
            }
        }
        if (key.equals("ufr.id")) {
            JSONArray jsonArray = parameter.getJSONArray("value");
            List<String> clauseOr = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                clauseOr.add("(c.ufr.id.code = :codeUfr" + i + " AND c.ufr.id.codeUniversite = :codeUnivUfr" + i + ")");
            }
            clauses.add("(" + String.join(" OR ", clauseOr) + ")");
        }
        if (key.equals("etudiant")) {
            String value = parameter.getString("value").toLowerCase();
            String[] parts = value.split(" ");
            String clause = "LOWER(c.etudiant.identEtudiant) LIKE :etudiant OR LOWER(c.etudiant.nom) LIKE :etudiant OR LOWER(c.etudiant.prenom) LIKE :etudiant OR" +
                    " LOWER(c.etudiant.mail) LIKE :etudiant OR LOWER(c.etudiant.numEtudiant) LIKE :etudiant";
            StringBuilder nom = new StringBuilder();
            StringBuilder prenom = new StringBuilder();

            for (int i = 0; i < parts.length; ++i) {
                nom.append(" LOWER(c.etudiant.nom) LIKE :etudiantSplit").append(i).append(" OR");
                prenom.append(" LOWER(c.etudiant.prenom) LIKE :etudiantSplit").append(i).append(" OR");
            }
            nom = new StringBuilder("(" + nom.substring(0, nom.length() - 2) + ")");
            prenom = new StringBuilder("(" + prenom.substring(0, prenom.length() - 2) + ")");

            clauses.add("(" + clause + " OR (" + nom + " AND " + prenom + "))");
        }
        if (key.equals("enseignant")) {
            clauses.add("(LOWER(c.enseignant.uidEnseignant) LIKE :enseignant OR LOWER(c.enseignant.nom) LIKE :enseignant OR LOWER(c.enseignant.prenom) LIKE :enseignant OR LOWER(c.enseignant.mail) LIKE :enseignant)");
        }
        if (key.equals("avenant")) {
            if (parameter.getBoolean("value")) {
                clauses.add("(avenant.id IS NOT NULL)");
            } else {
                clauses.add("(avenant.id IS NULL)");
            }
        }
        if (key.equals("etatValidation")) {
            JSONArray jsonArray = parameter.getJSONArray("value");
            List<String> clauseAnd = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                String code = jsonArray.getString(i);
                switch (code) {
                    case "validationPedagogique":
                        clauseAnd.add("c.validationPedagogique = TRUE");
                        break;
                    case "validationConvention":
                        clauseAnd.add("c.validationConvention = TRUE");
                        break;
                    case "nonValidationPedagogique":
                        clauseAnd.add("c.validationPedagogique = FALSE");
                        break;
                    case "nonValidationConvention":
                        clauseAnd.add("c.validationConvention = FALSE");
                        break;
                    default:
                        break;
                }
            }
            if (clauseAnd.size() > 0) {
                clauses.add("(" + String.join(" AND ", clauseAnd) + ")");
            }
        }
        if (key.equals("etatGestionnaire")) {
            clauses.add("c.validationConvention = FALSE");
            clauses.add("c.validationPedagogique = TRUE");
        }
        if (key.equals("isConventionValide")) {
            clauses.add(" (c.centreGestion.validationPedagogique = FALSE OR c.validationPedagogique = TRUE) AND" +
                    " (c.centreGestion.validationConvention = FALSE OR c.validationConvention = TRUE)");
        }
        if (key.equals("lieuStage")) {
            List<String> clauseOr = new ArrayList<>();
            clauseOr.add("LOWER(c.service.nom) = :lieuStage");
            clauseOr.add("LOWER(c.service.commune) = :lieuStage");
            clauseOr.add("LOWER(c.service.pays.lib) = :lieuStage");
            clauses.add("(" + String.join(" OR ", clauseOr) + ")");
        }
        if (key.equals("structure")) {
            clauses.add("(LOWER(c.structure.raisonSociale) LIKE :structure OR LOWER(c.structure.numeroSiret) LIKE :structure)");
        }
        if (key.equals("stageTermine")) {
            clauses.add("((FALSE = :stageTermine) OR c.dateFinStage < CURDATE())");
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JSONObject parameter, Query query) {
        if (key.equals("centreGestion.personnels")) {
            query.setParameter(key.replace(".", ""), parameter.getString("value"));
        }
        if (key.equals("enseignant.uidEnseignant")) {
            query.setParameter(key.replace(".", ""), parameter.getString("value"));
        }
        if (key.equals("etudiant.identEtudiant")) {
            query.setParameter(key.replace(".", ""), parameter.getString("value"));
        }
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
        if (key.equals("etudiant")) {
            String value = parameter.getString("value").toLowerCase();
            query.setParameter("etudiant", "%" + value + "%");
            String[] parts = value.split(" ");
            for (int i = 0; i < parts.length; i++) {
                query.setParameter("etudiantSplit" + i, "%" + parts[i] + "%");
            }
        }
        if (key.equals("enseignant")) {
            query.setParameter("enseignant", "%" + parameter.getString("value").toLowerCase() + "%");
        }
        if (key.equals("lieuStage")) {
            query.setParameter("lieuStage", "%" + parameter.getString("value").toLowerCase() + "%");
        }
        if (key.equals("structure")) {
            query.setParameter("structure", "%" + parameter.getString("value").toLowerCase() + "%");
        }
        if (key.equals("stageTermine")) {
            query.setParameter("stageTermine", parameter.getBoolean("value"));
        }
    }

    @Override
    protected Map<String, String> orderBy(String predicate, String sortOrder) {
        Map<String, String> predicates = super.orderBy(predicate, sortOrder);
        // Ajout d'un tri sur le nom, prénom, l'id DESC si non présent
        if (!predicates.containsKey("etudiant.nom")) {
            predicates.put("etudiant.nom", "ASC");
        }
        if (!predicates.containsKey("etudiant.prenom")) {
            predicates.put("etudiant.prenom", "ASC");
        }
        if (!predicates.containsKey("id")) {
            predicates.put("id", "DESC");
        }
        return predicates;
    }
}
