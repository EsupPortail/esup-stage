package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.NafN5;
import org.esup_portail.esup_stage.model.StatutJuridique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StatutJuridiqueJpaRepository extends JpaRepository<StatutJuridique, Integer> {

    StatutJuridique findById(int id);

    @Query("SELECT s FROM StatutJuridique s WHERE s.libelle = :lib")
    StatutJuridique findByLibelle(String lib);
}
