package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StructureJpaRepository extends JpaRepository<Structure, Integer> {

    @Query("SELECT s FROM Structure s WHERE s.id = :id")
    Structure findById(@Param("id") int id);

    @Query("SELECT s FROM Structure s WHERE s.numeroRNE = :rne")
    Structure findByRNE(@Param("rne") String rne);

    @Query("SELECT s FROM Structure s WHERE s.numeroSiret = :siret")
    Structure findBySiret(@Param("siret") String siret);

    @Query("SELECT s FROM Structure s WHERE s.raisonSociale = :raisonSociale")
    Structure findByRaisonSociale(String raisonSociale);
}
