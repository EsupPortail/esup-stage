package org.esup_portail.esup_stage.repository;

import tools.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.esup_portail.esup_stage.model.PersonnelCentreGestion;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class PersonnelCentreGestionRepository extends PaginationRepository<PersonnelCentreGestion> {

    public PersonnelCentreGestionRepository(EntityManager em) {
        super(em, PersonnelCentreGestion.class, "pc");
        this.predicateWhitelist = Arrays.asList("civilite.libelle", "nom", "prenom", "droitAdministration.libelle", "alertesMail");
        this.specificFilterWhitelist = Arrays.asList("centreIds");
    }
    @Override
    protected void addSpecificParameter(String key, JsonNode parameter, java.util.List<String> clauses) {
        if (key.equals("centreIds")) {
            clauses.add(getJsonArrayValues(parameter).isEmpty() ? "1 = 0" : "pc.centreGestion.id IN :centreIds");
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JsonNode parameter, Query query) {
        if (key.equals("centreIds") && !getJsonArrayValues(parameter).isEmpty()) {
            query.setParameter("centreIds", getJsonArrayValues(parameter));
        }
    }
}
