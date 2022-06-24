package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.HistoriqueMailGroupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueMailGroupeJpaRepository extends JpaRepository<HistoriqueMailGroupe, Integer> {

    HistoriqueMailGroupe findById(int id);

    @Query("SELECT hmg FROM HistoriqueMailGroupe hmg WHERE hmg.groupeEtudiant.id = :idGroupeEtudiant ORDER BY hmg.date DESC")
    List<HistoriqueMailGroupe> findByGroupeEtudiant(int idGroupeEtudiant);
}