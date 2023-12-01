package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.PeriodeInterruptionAvenant;
import org.esup_portail.esup_stage.model.PeriodeInterruptionStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeriodeInterruptionAvenantJpaRepository extends JpaRepository<PeriodeInterruptionAvenant, Integer> {

    PeriodeInterruptionAvenant findById(int id);

    @Query("SELECT pia FROM PeriodeInterruptionAvenant pia WHERE pia.avenant.id = :idAvenant ORDER BY pia.dateDebutInterruption ASC")
    List<PeriodeInterruptionAvenant> findByAvenant(int idAvenant);
}