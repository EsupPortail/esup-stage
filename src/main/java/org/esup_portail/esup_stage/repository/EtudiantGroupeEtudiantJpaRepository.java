package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.EtudiantGroupeEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtudiantGroupeEtudiantJpaRepository extends JpaRepository<EtudiantGroupeEtudiant, Integer> {

    @Query("SELECT ege FROM EtudiantGroupeEtudiant ege WHERE ege.etudiant.id = :idEtudiant and ege.groupeEtudiant.id = :idGroupeEtudiant")
    EtudiantGroupeEtudiant findByEtudiantAndGroupe(@Param("idEtudiant") int idEtudiant, @Param("idGroupeEtudiant") int idGroupeEtudiant);

    @Query("SELECT ege FROM EtudiantGroupeEtudiant ege LEFT JOIN ege.mergedConvention mc WHERE ege.convention.id = :idConvention OR mc.id = :idConvention")
    List<EtudiantGroupeEtudiant> findByConventionOrMergedConvention(@Param("idConvention") int idConvention);
}