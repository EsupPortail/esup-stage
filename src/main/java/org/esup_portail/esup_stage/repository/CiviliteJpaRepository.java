package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Civilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CiviliteJpaRepository extends JpaRepository<Civilite, Integer> {

    @Query("SELECT c FROM Civilite c WHERE c.id = :id")
    Civilite findById(int id);

}
