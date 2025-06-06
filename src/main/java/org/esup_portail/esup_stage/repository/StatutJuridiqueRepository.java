package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.StatutJuridique;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class StatutJuridiqueRepository extends PaginationRepository<StatutJuridique> {

    public StatutJuridiqueRepository(EntityManager em) {
        super(em, StatutJuridique.class, "sj");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
