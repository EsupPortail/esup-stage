package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.NiveauCentre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NiveauCentreJpaRepository extends JpaRepository<NiveauCentre, Integer> {

    List<NiveauCentre> findAll();
}
