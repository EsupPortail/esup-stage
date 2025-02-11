package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.NafN5;
import org.esup_portail.esup_stage.model.Pays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NafN5JpaRepository extends JpaRepository<NafN5, String> {

    @Query("SELECT n FROM NafN5 n WHERE n.code = :code")
    NafN5 findByCode(@Param("code") String code);

    @Query("SELECT n FROM NafN5 n WHERE n.libelle = :lib")
    NafN5 findByLibelle(@Param("lib") String lib);

    List<NafN5> findAll();
}
