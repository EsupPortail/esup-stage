package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StructureJpaRepository extends JpaRepository<Structure, Integer> {

    @Query("SELECT s FROM Structure s WHERE s.id = :id")
    Structure findById(@Param("id") int id);

    @Query("SELECT s FROM Structure s WHERE s.numeroRNE = :rne AND s.temEnServStructure = true")
    Structure findByRNE(@Param("rne") String rne);

    @Query("SELECT s FROM Structure s WHERE s.numeroSiret = :siret AND s.temEnServStructure = true")
    Structure findBySiret(@Param("siret") String siret);

    @Query("SELECT s FROM Structure s WHERE s.raisonSociale = :raisonSociale AND s.temEnServStructure = true")
    Structure findByRaisonSociale(@Param("raisonSociale") String raisonSociale);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Structure s WHERE s.numeroRNE = :rne AND s.temEnServStructure = true")
    Boolean existAndActifByNumeroRNE (@Param("rne") String rne);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Structure s WHERE s.numeroSiret = :siret AND s.temEnServStructure = true")
    Boolean existAndActifByNumeroSiret (@Param("siret") String siret);

}
