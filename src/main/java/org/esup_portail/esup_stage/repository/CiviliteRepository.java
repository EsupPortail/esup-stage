package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.Civilite;
import org.springframework.stereotype.Repository;

@Repository
public class CiviliteRepository extends PaginationRepository<Civilite> {
    public CiviliteRepository(EntityManager em) {
        super(em, Civilite.class, "c");
    }

}
