package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.HistoriqueStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueStructureRepository extends JpaRepository<HistoriqueStructure, Long> {
    List<HistoriqueStructure> findByStructureIdOrderByOperationDateDesc(Long structureId);

}
