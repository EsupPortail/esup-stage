package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.HistoriqueValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HistoriqueValidationJpaRepository extends JpaRepository<HistoriqueValidation, Integer> {

    @Query("SELECT hv FROM HistoriqueValidation hv WHERE hv.convention.id = :idConvention ORDER BY hv.date DESC")
    List<HistoriqueValidation> findByConvention(int idConvention);
}
