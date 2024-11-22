package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.ModeValidationStage;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class ModeValidationStageRepository extends PaginationRepository<ModeValidationStage> {

    public ModeValidationStageRepository(EntityManager em) {
        super(em, ModeValidationStage.class, "mv");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
