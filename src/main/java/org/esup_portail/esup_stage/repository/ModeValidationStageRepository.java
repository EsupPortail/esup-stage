package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.ModeValidationStage;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class ModeValidationStageRepository extends PaginationRepository<ModeValidationStage> {

    public ModeValidationStageRepository(EntityManager em) {
        super(em, ModeValidationStage.class, "mv");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
