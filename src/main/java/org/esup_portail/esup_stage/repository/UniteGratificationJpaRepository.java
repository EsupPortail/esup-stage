package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.UniteGratification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniteGratificationJpaRepository extends JpaRepository<UniteGratification, Integer> {

    UniteGratification findById(int id);
}
