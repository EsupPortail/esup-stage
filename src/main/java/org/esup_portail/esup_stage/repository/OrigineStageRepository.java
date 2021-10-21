package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.OrigineStage;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class OrigineStageRepository extends PaginationRepository<OrigineStage> {

    public OrigineStageRepository(EntityManager em) {
        super(em, OrigineStage.class, "os");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
