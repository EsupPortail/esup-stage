package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.PeriodeInterruptionStage;
import org.springframework.stereotype.Repository;

@Repository
public class PeriodeInterruptionStageRepository extends PaginationRepository<PeriodeInterruptionStage> {

    public PeriodeInterruptionStageRepository(EntityManager em) {
        super(em, PeriodeInterruptionStage.class, "pis");
    }
}