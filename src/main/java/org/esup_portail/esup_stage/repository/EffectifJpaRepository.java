package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Effectif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EffectifJpaRepository extends JpaRepository<Effectif, Integer> {

    @Query("SELECT e FROM Effectif e WHERE e.temEnServ = 'O'")
    List<Effectif> findAllActif();

    @Query("SELECT e FROM Effectif e WHERE e.libelle = :lib")
    Effectif findByLibelle(String lib);

    Effectif findById(int id);
}
