package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.Devise;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class DeviseRepository extends PaginationRepository<Devise> {

    public DeviseRepository(EntityManager em) {
        super(em, Devise.class, "d");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
