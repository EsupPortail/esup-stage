package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.PeriodeInterruptionStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface PeriodeInterruptionStageJpaRepository extends JpaRepository<PeriodeInterruptionStage, Integer> {

    PeriodeInterruptionStage findById(int id);

    @Query("SELECT pis FROM PeriodeInterruptionStage pis WHERE pis.convention.id = :idConvention ORDER BY pis.dateDebutInterruption ASC")
    List<PeriodeInterruptionStage> findByConvention(int idConvention);

    @Transactional
    @Modifying
    @Query("DELETE FROM PeriodeInterruptionStage pis WHERE pis.convention.id = :idConvention")
    void deleteByConvention(int idConvention);
}