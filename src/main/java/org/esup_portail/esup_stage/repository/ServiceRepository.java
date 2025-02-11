package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Service;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

@Repository
public class ServiceRepository extends PaginationRepository<Service> {
    public ServiceRepository(EntityManager em) {
        super(em, Service.class, "s");
    }

}
