package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.PeriodeStage;
import org.springframework.stereotype.Repository;

@Repository
public class PeriodeStageRepository extends PaginationRepository<PeriodeStage> {

    public PeriodeStageRepository(EntityManager em) {
        super(em, PeriodeStage.class, "ps");
    }
}