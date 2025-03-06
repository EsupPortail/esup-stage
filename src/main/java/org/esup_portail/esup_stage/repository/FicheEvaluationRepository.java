package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.FicheEvaluation;
import org.springframework.stereotype.Repository;

@Repository
public class FicheEvaluationRepository extends PaginationRepository<FicheEvaluation> {

    public FicheEvaluationRepository(EntityManager em) {
        super(em, FicheEvaluation.class, "fe");
    }
}