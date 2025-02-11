package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.FicheEvaluation;
import org.esup_portail.esup_stage.model.ReponseSupplementaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReponseSupplementaireJpaRepository extends JpaRepository<ReponseSupplementaire, Integer> {

    ReponseSupplementaire findById(int id);

    @Query("SELECT re FROM ReponseSupplementaire re WHERE re.convention.id = :idConvention AND re.questionSupplementaire.id = :idQuestionSupplementaire ")
    ReponseSupplementaire findByQuestionAndConvention(@Param("idConvention") int idConvention, @Param("idQuestionSupplementaire") int idQuestionSupplementaire);

}