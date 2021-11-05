package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.CritereGestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CritereGestionJpaRepository extends JpaRepository<CritereGestion, Integer> {

    @Query("SELECT cg FROM CritereGestion cg WHERE cg.centreGestion.id = :id")
    CritereGestion findByCentreId(int id);

    @Query("SELECT cg FROM CritereGestion cg WHERE cg.id.codeVersionEtape = ''")
    List<CritereGestion> findComposantes();

    @Query("SELECT cg FROM CritereGestion cg WHERE cg.id.codeVersionEtape != ''")
    List<CritereGestion> findEtapes();

    @Query("SELECT cg FROM CritereGestion cg WHERE cg.id.code = :code AND cg.id.codeVersionEtape = :codeVersionEtape")
    CritereGestion findEtapeById(String code, String codeVersionEtape);
}
