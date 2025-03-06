package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.PeriodeInterruptionAvenant;
import org.springframework.stereotype.Repository;

@Repository
public class PeriodeInterruptionAvenantRepository extends PaginationRepository<PeriodeInterruptionAvenant> {

    public PeriodeInterruptionAvenantRepository(EntityManager em) {
        super(em, PeriodeInterruptionAvenant.class, "pia");
    }
}