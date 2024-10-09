package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.PersonnelCentreGestion;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class PersonnelCentreGestionRepository extends PaginationRepository<PersonnelCentreGestion> {

    public PersonnelCentreGestionRepository(EntityManager em) {
        super(em, PersonnelCentreGestion.class, "pc");
        this.predicateWhitelist = Arrays.asList("civilite.libelle", "nom", "prenom", "droitAdministration.libelle", "alertesMail");
    }
}
