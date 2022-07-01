package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.enums.AppConfigCodeEnum;
import org.esup_portail.esup_stage.model.AppConfig;
import org.esup_portail.esup_stage.model.Pays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaysJpaRepository extends JpaRepository<Pays, Integer> {

    Pays findById(int id);

    @Query("SELECT p FROM Pays p WHERE p.lib = :lib")
    Pays findByLibelle(String lib);
}
