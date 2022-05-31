package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.GroupeEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupeEtudiantJpaRepository extends JpaRepository<GroupeEtudiant, Integer> {

    GroupeEtudiant findById(int id);

    @Query("SELECT c FROM GroupeEtudiant ge WHERE ge.loginCreation = :login")
    GroupeEtudiant findBrouillon(String login);
}