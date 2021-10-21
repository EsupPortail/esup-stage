package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.ModeVersGratification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModeVersGratificationJpaRepository extends JpaRepository<ModeVersGratification, Integer> {

    ModeVersGratification findById(int id);
}
