package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.Maintenance;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class MaintenanceRepository extends PaginationRepository<Maintenance> {

    public MaintenanceRepository(EntityManager em) {
        super(em, Maintenance.class,"m");
        this.predicateWhitelist = Arrays.asList("id", "datDebMaint", "datFinMaint", "datAlertMaint", "message", "createdBy", "createdAt");
    }
}
