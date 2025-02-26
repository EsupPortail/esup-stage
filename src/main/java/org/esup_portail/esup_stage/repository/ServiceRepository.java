package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.Service;
import org.springframework.stereotype.Repository;

@Repository
public class ServiceRepository extends PaginationRepository<Service> {
    public ServiceRepository(EntityManager em) {
        super(em, Service.class, "s");
    }

}
