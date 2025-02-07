package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Utilisateur;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class UtilisateurRepository extends PaginationRepository<Utilisateur> {

    public UtilisateurRepository(EntityManager em) {
        super(em, Utilisateur.class, "u");
        this.predicateWhitelist = Arrays.asList("login", "nom", "prenom", "actif");
    }

    @Override
    protected void addSpecificParameter(String key, JSONObject parameter, List<String> clauses) {
        if (key.equals("utilisateur")) {
            clauses.add("(LOWER(u.login) LIKE :" + key.replace(".", "") + " OR LOWER(u.nom) LIKE :" + key.replace(".", "") + " OR LOWER(u.prenom) LIKE :" + key.replace(".", "") + ")");
        }
        if (key.equals("roles")) {
            addJoins("JOIN u.roles r");
            clauses.add("r.id IN :" + key.replace(".", ""));
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JSONObject parameter, Query query) {
        if (key.equals("utilisateur")) {
            query.setParameter(key.replace(".", ""), "%" + parameter.getString("value").toLowerCase() + "%");
        }
        if (key.equals("roles")) {
            List<Object> values = new ArrayList<>();
            JSONArray jsonArray = parameter.getJSONArray("value");
            for (int j = 0; j < jsonArray.length(); ++j) {
                values.add(jsonArray.get(j));
            }
            query.setParameter(key.replace(".", ""), values);
        }
    }
}
