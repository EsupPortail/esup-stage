package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Affectation;
import org.esup_portail.esup_stage.model.AffectationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AffectationJpaRepository extends JpaRepository<Affectation, AffectationId> {

    @Query("SELECT a FROM Affectation a WHERE a.id.codeUniversite = :codeUniversite")
    List<Affectation> findByCodeUniversite(@Param("codeUniversite") String codeUniversite);
}
