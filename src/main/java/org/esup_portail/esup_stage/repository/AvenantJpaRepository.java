package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.Convention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvenantJpaRepository extends JpaRepository<Avenant, Integer> {

    Avenant findById(int id);

    @Query("SELECT a FROM Avenant a WHERE a.convention.id = :idConvention")
    List<Avenant> findByConvention(@Param("idConvention") int idConvention);

    @Query("SELECT a FROM Avenant a WHERE a.temAvenantSigne = false AND a.validationAvenant = true AND a.dateEnvoiSignature IS NOT NULL")
    List<Avenant> findAvenantNonSignes();
}