package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.FicheEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FicheEvaluationJpaRepository extends JpaRepository<FicheEvaluation, Integer> {

    FicheEvaluation findById(int id);

    @Query("SELECT fe FROM FicheEvaluation fe WHERE fe.centreGestion.id = :idCentreGestion")
    FicheEvaluation findByCentreGestion(@Param("idCentreGestion") int idCentreGestion);

}