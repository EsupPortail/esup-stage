package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EtudiantJpaRepository extends JpaRepository<Etudiant, Integer> {

    @Query("SELECT e FROM Etudiant e WHERE e.identEtudiant = :login")
    Etudiant findByLogin(String login);
}
