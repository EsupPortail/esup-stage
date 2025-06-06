package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.Avenant;
import org.springframework.stereotype.Repository;

@Repository
public class AvenantRepository extends PaginationRepository<Avenant> {

    public AvenantRepository(EntityManager em) {
        super(em, Avenant.class, "a");
    }
}