package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.NatureTravail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NatureTravailJpaRepository extends JpaRepository<NatureTravail, Integer> {

    NatureTravail findById(int id);
}