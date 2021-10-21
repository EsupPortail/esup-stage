package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.NiveauFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NiveauFormationJpaRepository extends JpaRepository<NiveauFormation, Integer> {

    NiveauFormation findById(int id);
}
