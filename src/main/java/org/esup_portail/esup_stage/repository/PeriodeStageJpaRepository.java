package org.esup_portail.esup_stage.repository;

import jakarta.transaction.Transactional;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.PeriodeStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeriodeStageJpaRepository extends JpaRepository<PeriodeStage, Integer> {

    PeriodeStage findById(int id);

    List<PeriodeStage> findByConvention(Convention convention);

    @Transactional
    @Modifying
    @Query("DELETE FROM PeriodeStage ps WHERE ps.convention.id = :idConvention")
    void deleteByConvention(@Param("idConvention") int idConvention);
}