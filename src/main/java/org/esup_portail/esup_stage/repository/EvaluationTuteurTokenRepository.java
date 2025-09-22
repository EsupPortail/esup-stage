package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.EvaluationTuteurToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationTuteurTokenRepository extends JpaRepository<EvaluationTuteurToken, Integer> {

    EvaluationTuteurToken findById(int id);

    EvaluationTuteurToken findByToken(String token);

    @Query("SELECT t FROM EvaluationTuteurToken t WHERE t.contact.id = :tuteurId")
    List<EvaluationTuteurToken> findByTuteurId(Integer tuteurId);
}
