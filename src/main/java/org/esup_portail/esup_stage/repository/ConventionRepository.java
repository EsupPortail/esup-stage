package org.esup_portail.esup_stage.repository;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.esup_portail.esup_stage.model.Convention;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public class ConventionRepository extends PaginationRepository<Convention> {

    public ConventionRepository(EntityManager em) {
        super(em, Convention.class, "c");
        this.predicateWhitelist = Arrays.asList("id", "etudiant.nom", "etudiant.prenom", "etudiant.nom_etudiant.prenom", "structure.raisonSociale", "dateDebutStage", "dateFinStage", "ufr.libelle", "etape.libelle", "enseignant.prenom", "sujetStage", "lieuStage", "annee");
        this.specificFilterWhitelist = Arrays.asList("centreGestion.personnels", "enseignant.uidEnseignant", "etudiant.identEtudiant", "etape.id", "ufr.id", "etudiant", "enseignant", "avenant", "etatValidation", "etatGestionnaire", "isConventionValide", "lieuStage", "structure", "stageTermine");
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
    protected void addSpecificParameter(String key, JsonNode parameter, List<String> clauses) {
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
            JsonNode jsonArray = parameter.get("value");
            List<String> clauseOr = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); ++i) {
                clauseOr.add("(c.etape.id.code = :codeEtape" + i + " AND c.etape.id.codeUniversite = :codeUnivEtape" + i + " AND c.etape.id.codeVersionEtape = :versionEtape" + i + ")");
            }
            if (clauseOr.isEmpty()) {
                clauses.add("(" + String.join(" OR ", clauseOr) + ")");
            }
        }
        if (key.equals("ufr.id")) {
            JsonNode jsonArray = parameter.get("value");
            List<String> clauseOr = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); ++i) {
                clauseOr.add("(c.ufr.id.code = :codeUfr" + i + " AND c.ufr.id.codeUniversite = :codeUnivUfr" + i + ")");
            }
            clauses.add("(" + String.join(" OR ", clauseOr) + ")");
        }
        if (key.equals("etudiant")) {
            String value = getJsonTextValue(parameter).toLowerCase();
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
            if (getJsonBooleanValue(parameter)) {
                clauses.add("(avenant.id IS NOT NULL)");
            } else {
                clauses.add("(avenant.id IS NULL)");
            }
        }
        if (key.equals("etatValidation")) {
            JsonNode jsonArray = parameter.get("value");
            List<String> clauseAnd = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); ++i) {
                String code = jsonArray.get(i).asText();
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
                    case "signe":
                        clauseAnd.add("c.dateSignatureEnseignant IS NOT NULL AND c.dateSignatureEtudiant IS NOT NULL AND c.dateSignatureSignataire IS NOT NULL AND c.dateSignatureTuteur IS NOT NULL AND c.dateSignatureViseur IS NOT NULL");
                        break;
                    case "enCours":
                        clauseAnd.add("(c.dateSignatureEnseignant IS NOT NULL OR c.dateSignatureEtudiant IS NOT NULL OR c.dateSignatureSignataire IS NOT NULL OR c.dateSignatureTuteur IS NOT NULL OR c.dateSignatureViseur IS NOT NULL) AND (c.dateSignatureEnseignant IS NULL OR c.dateSignatureEtudiant IS NULL OR c.dateSignatureSignataire IS NULL OR c.dateSignatureTuteur IS NULL OR c.dateSignatureViseur IS NULL)");
                        break;
                    case "nonSigne":
                        clauseAnd.add("c.dateSignatureEnseignant IS NULL AND c.dateSignatureEtudiant IS NULL AND c.dateSignatureSignataire IS NULL AND c.dateSignatureTuteur IS NULL AND c.dateSignatureViseur IS NULL");
                        break;
                    default:
                        break;
                }
            }
            if (clauseAnd.isEmpty()) {
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
    protected void setSpecificParameterValue(String key, JsonNode parameter, Query query) {
        if (key.equals("centreGestion.personnels")) {
            query.setParameter(key.replace(".", ""), getJsonTextValue(parameter));
        }
        if (key.equals("enseignant.uidEnseignant")) {
            query.setParameter(key.replace(".", ""), getJsonTextValue(parameter));
        }
        if (key.equals("etudiant.identEtudiant")) {
            query.setParameter(key.replace(".", ""), getJsonTextValue(parameter));
        }
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
        if (key.equals("etudiant")) {
            String value = getJsonTextValue(parameter).toLowerCase();
            query.setParameter("etudiant", "%" + value + "%");
            String[] parts = value.split(" ");
            for (int i = 0; i < parts.length; i++) {
                query.setParameter("etudiantSplit" + i, "%" + parts[i] + "%");
            }
        }
        if (key.equals("enseignant")) {
            query.setParameter("enseignant", "%" + getJsonTextValue(parameter).toLowerCase() + "%");
        }
        if (key.equals("lieuStage")) {
            query.setParameter("lieuStage", "%" + getJsonTextValue(parameter).toLowerCase() + "%");
        }
        if (key.equals("structure")) {
            query.setParameter("structure", "%" + getJsonTextValue(parameter).toLowerCase() + "%");
        }
        if (key.equals("stageTermine")) {
            query.setParameter("stageTermine", getJsonBooleanValue(parameter));
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
