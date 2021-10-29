package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Civilite;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class CiviliteRepository extends PaginationRepository<Civilite> {
    public CiviliteRepository(EntityManager em) {
        super(em, Civilite.class, "c");
    }

}
