package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.HistoriqueStructure;
import org.esup_portail.esup_stage.model.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueStructureJpaRepository extends JpaRepository<HistoriqueStructure, Integer> {

    List<HistoriqueStructure> findByStructure(Structure structure);
}