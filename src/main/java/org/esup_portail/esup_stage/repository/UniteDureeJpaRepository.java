package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.UniteDuree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniteDureeJpaRepository extends JpaRepository<UniteDuree, Integer> {

    UniteDuree findById(int id);
}
