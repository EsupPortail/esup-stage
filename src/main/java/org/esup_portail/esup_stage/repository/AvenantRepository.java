package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Avenant;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class AvenantRepository extends PaginationRepository<Avenant> {

    public AvenantRepository(EntityManager em) {super(em, Avenant.class, "a");}
}