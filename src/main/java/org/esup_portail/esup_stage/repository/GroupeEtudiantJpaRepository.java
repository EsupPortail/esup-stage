package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.GroupeEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupeEtudiantJpaRepository extends JpaRepository<GroupeEtudiant, Integer> {

    GroupeEtudiant findById(int id);

    @Query("SELECT ge FROM GroupeEtudiant ge WHERE ge.loginCreation = :login AND ge.validationCreation = FALSE")
    GroupeEtudiant findBrouillon(@Param("login") String login);

    @Query("SELECT ge FROM GroupeEtudiant ge WHERE ge.convention.id = :idConvention")
    List<GroupeEtudiant> findByConventionId(@Param("idConvention") int idConvention);
}