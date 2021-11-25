package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Avenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvenantJpaRepository extends JpaRepository<Avenant, Integer> {

    Avenant findById(int id);

    @Query("SELECT a FROM Avenant a WHERE a.convention.id = :idConvention")
    List<Avenant> findByConvention(int idConvention);
}