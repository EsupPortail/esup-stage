package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StructureJpaRepository extends JpaRepository<Structure, Integer> {

    @Query("SELECT s FROM Structure s WHERE s.id = :id")
    Structure findById(int id);

    @Query("SELECT s FROM Structure s WHERE s.numeroRNE = :rne")
    Structure findByRNE(String rne);

    @Query("SELECT s FROM Structure s WHERE s.numeroSiret = :siret")
    Structure findBySiret(String siret);
}
