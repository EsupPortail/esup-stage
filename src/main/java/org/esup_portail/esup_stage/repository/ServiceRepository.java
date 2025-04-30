package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.Service;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class ServiceRepository extends PaginationRepository<Service> {
    public ServiceRepository(EntityManager em) {
        super(em, Service.class, "s");
        this.predicateWhitelist = Arrays.asList( "nom","pays.lib", "commune", "voie");
    }

}
