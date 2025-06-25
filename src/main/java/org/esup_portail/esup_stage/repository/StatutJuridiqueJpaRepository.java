package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.StatutJuridique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatutJuridiqueJpaRepository extends JpaRepository<StatutJuridique, Integer> {

    StatutJuridique findById(int id);

    @Query("SELECT s FROM StatutJuridique s WHERE s.libelle = :lib")
    StatutJuridique findByLibelle(@Param("lib") String lib);

    @Query("SELECT s FROM StatutJuridique s WHERE s.code = :code")
    StatutJuridique findByCode(@Param("code")String code);

    @Query("SELECT s.code FROM StatutJuridique s WHERE s.id IN :ids")
    List<String> findCodeByIdIn(@Param("ids") List<Integer> ids);

}
