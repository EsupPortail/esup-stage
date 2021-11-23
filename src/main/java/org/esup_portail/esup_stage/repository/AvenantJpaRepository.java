package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Avenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvenantJpaRepository extends JpaRepository<Avenant, Integer> {

    Avenant findById(int id);
}