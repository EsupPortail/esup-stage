package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.QuestionEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionEvaluationJpaRepository extends JpaRepository<QuestionEvaluation, Integer> {

    QuestionEvaluation findByid(@Param("id")Integer id);

    @Query("SELECT q FROM QuestionEvaluation q WHERE q.code = :code")
    QuestionEvaluation findByCode(@Param("code") String code);

    @Query("SELECT q FROM QuestionEvaluation q WHERE q.code LIKE CONCAT(:code, '%')")
    List<QuestionEvaluation> findByCodeContaining(@Param("code") String code);

}
