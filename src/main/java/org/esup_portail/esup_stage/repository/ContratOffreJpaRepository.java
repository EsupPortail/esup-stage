package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.ContratOffre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratOffreJpaRepository extends JpaRepository<ContratOffre, Integer> {
    ContratOffre findById(int id);
}
