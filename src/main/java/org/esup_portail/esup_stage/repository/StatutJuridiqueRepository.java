package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.StatutJuridique;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class StatutJuridiqueRepository extends PaginationRepository<StatutJuridique> {

    public StatutJuridiqueRepository(EntityManager em) {
        super(em, StatutJuridique.class, "sj");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
