package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommuneJpaRepository extends JpaRepository<Commune, Integer> {

    Commune findById(int id);

    List<Commune> findAll();
}
