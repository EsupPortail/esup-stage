package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface StructureJpaRepository extends JpaRepository<Structure, Integer> {

    @Query("SELECT s FROM Structure s WHERE s.id = :id")
    Structure findById(@Param("id") int id);

    @Query("SELECT s FROM Structure s WHERE s.numeroRNE = :rne AND s.temEnServStructure = true")
    Structure findByRNE(@Param("rne") String rne);

    @Query("SELECT s FROM Structure s WHERE s.numeroSiret = :siret AND s.temEnServStructure = true")
    Structure findBySiret(@Param("siret") String siret);

    @Query("SELECT s FROM Structure s WHERE s.raisonSociale = :raisonSociale AND s.temEnServStructure = true")
    Structure findByRaisonSociale(@Param("raisonSociale") String raisonSociale);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Structure s WHERE s.numeroRNE = :rne AND s.temEnServStructure = true")
    Boolean existAndActifByNumeroRNE (@Param("rne") String rne);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Structure s WHERE s.numeroSiret = :siret AND s.temEnServStructure = true")
    Boolean existAndActifByNumeroSiret (@Param("siret") String siret);

    @Query("""
    SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
    FROM Structure s, Utilisateur u
    WHERE s.id = :id
      AND u.id = :userId
      AND s.loginCreation = u.login
      AND (s.loginModif IS NULL OR s.loginModif = s.loginCreation)
    """)
    boolean isOwner(@Param("id") Integer id, @Param("userId") int userId);

    @Transactional
    @Modifying
    @Query("UPDATE Structure s SET s.dateArchivage = :dateArchivage WHERE s.dateArchivage IS NULL" +
            " AND EXISTS (SELECT c1.id FROM Convention c1 WHERE c1.structure = s)" +
            " AND NOT EXISTS (SELECT c2.id FROM Convention c2 WHERE c2.structure = s AND c2.dateArchivage IS NULL)")
    int archiverStructuresSansConventionActive(@Param("dateArchivage") Date dateArchivage);

    @Transactional
    @Modifying
    @Query("UPDATE Structure s SET s.dateArchivage = NULL WHERE s.dateArchivage IS NOT NULL" +
            " AND EXISTS (SELECT c.id FROM Convention c WHERE c.structure = s AND c.dateArchivage IS NULL)")
    int desarchiverStructuresReutilisees();

    @Query("SELECT s.id FROM Structure s WHERE s.dateArchivage IS NOT NULL AND s.dateArchivage < :seuil")
    List<Integer> findIdsStructuresAPurger(@Param("seuil") Date seuil);
}
