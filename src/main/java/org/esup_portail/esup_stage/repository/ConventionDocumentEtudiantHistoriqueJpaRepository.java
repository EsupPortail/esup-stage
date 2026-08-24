package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.ConventionDocumentEtudiantHistorique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConventionDocumentEtudiantHistoriqueJpaRepository extends JpaRepository<ConventionDocumentEtudiantHistorique, Integer> {

    @Modifying
    @Query("DELETE FROM ConventionDocumentEtudiantHistorique h WHERE h.idConvention = :idConvention")
    int deleteByConventionId(@Param("idConvention") int idConvention);
}
