package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.OrigineStage;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class OrigineStageRepository extends PaginationRepository<OrigineStage> {

    public OrigineStageRepository(EntityManager em) {
        super(em, OrigineStage.class, "os");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
