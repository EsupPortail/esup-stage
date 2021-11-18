package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EnseignantJpaRepository extends JpaRepository<Enseignant, Integer> {

    Enseignant findById(int id);

    @Query("SELECT e FROM Enseignant e WHERE e.uidEnseignant = :uid")
    Enseignant findByUid(String uid);

}