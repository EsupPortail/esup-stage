package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.CritereGestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CritereGestionJpaRepository extends JpaRepository<CritereGestion, Integer> {

    @Query("SELECT cg FROM CritereGestion cg WHERE cg.centreGestion.id = :id")
    List<CritereGestion> findByCentreId(@Param("id") int id);

    @Query("SELECT cg FROM CritereGestion cg WHERE cg.id.codeVersionEtape = ''")
    List<CritereGestion> findComposantes();

    @Query("SELECT cg FROM CritereGestion cg WHERE cg.id.codeVersionEtape != ''")
    List<CritereGestion> findEtapes();

    @Query("SELECT cg FROM CritereGestion cg WHERE cg.id.code = :code AND cg.id.codeVersionEtape = :codeVersionEtape")
    CritereGestion findEtapeById(@Param("code") String code, @Param("codeVersionEtape") String codeVersionEtape);

    @Query("SELECT cg FROM CritereGestion cg WHERE cg.centreGestion.id = :id AND cg.id.codeVersionEtape IS NOT NULL")
    List<CritereGestion> findEtapesByCentreId(@Param("id") int id);

    @Transactional
    @Modifying
    @Query("DELETE FROM CritereGestion cg WHERE cg.centreGestion.id = :id")
    void deleteCriteresByCentreId(@Param("id") int id);
}
