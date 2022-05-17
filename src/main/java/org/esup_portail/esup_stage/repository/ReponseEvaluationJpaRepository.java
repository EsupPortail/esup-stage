package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.FicheEvaluation;
import org.esup_portail.esup_stage.model.ReponseEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReponseEvaluationJpaRepository extends JpaRepository<ReponseEvaluation, Integer> {

    ReponseEvaluation findById(int id);

    @Query("SELECT re FROM ReponseEvaluation re WHERE re.convention.id = :idConvention")
    ReponseEvaluation findByConvention(int idConvention);
}