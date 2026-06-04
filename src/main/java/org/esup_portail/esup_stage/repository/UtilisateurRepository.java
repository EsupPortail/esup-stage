package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import com.fasterxml.jackson.databind.JsonNode;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class UtilisateurRepository extends PaginationRepository<Utilisateur> {

    public UtilisateurRepository(EntityManager em) {
        super(em, Utilisateur.class, "u");
        this.predicateWhitelist = Arrays.asList("login", "nom", "prenom", "actif");
        this.specificFilterWhitelist = Arrays.asList("utilisateur", "roles");
    }

    @Override
    protected void addSpecificParameter(String key, JsonNode parameter, List<String> clauses) {
        if (key.equals("utilisateur")) {
            clauses.add("(LOWER(u.login) LIKE :" + key.replace(".", "") + " OR LOWER(u.nom) LIKE :" + key.replace(".", "") + " OR LOWER(u.prenom) LIKE :" + key.replace(".", "") + ")");
        }
        if (key.equals("roles")) {
            addJoins("JOIN u.roles r");
            clauses.add("r.id IN :" + key.replace(".", ""));
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JsonNode parameter, Query query) {
        if (key.equals("utilisateur")) {
            query.setParameter(key.replace(".", ""), "%" + getJsonTextValue(parameter).toLowerCase() + "%");
        }
        if (key.equals("roles")) {
            query.setParameter(key.replace(".", ""), getJsonArrayValues(parameter));
        }
    }
}
