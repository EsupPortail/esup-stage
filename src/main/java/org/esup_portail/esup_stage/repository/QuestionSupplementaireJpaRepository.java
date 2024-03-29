package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.QuestionSupplementaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionSupplementaireJpaRepository extends JpaRepository<QuestionSupplementaire, Integer> {

    QuestionSupplementaire findById(int id);

    @Query("SELECT qs FROM QuestionSupplementaire qs WHERE qs.ficheEvaluation.id = :idFicheEvaluation ORDER BY qs.idPlacement ASC")
    List<QuestionSupplementaire> findByFicheEvaluation(int idFicheEvaluation);
}