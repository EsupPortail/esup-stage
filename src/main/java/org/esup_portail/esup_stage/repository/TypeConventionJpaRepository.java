package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TypeConvention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeConventionJpaRepository extends JpaRepository<TypeConvention, Integer> {

    TypeConvention findById(int id);

    @Query("SELECT tc FROM TypeConvention tc WHERE LOWER(tc.codeCtrl) = LOWER(:codeCtrl)")
    TypeConvention findByCodeCtrl(@Param("codeCtrl") String codeCtrl);

    @Query(value = """
            SELECT tc.*
            FROM TypeConvention tc
            JOIN TypeConventionRegimeInscription tcri ON tcri.idTypeConvention = tc.idTypeConvention
            WHERE LOWER(tcri.codeRegime) = LOWER(:codeRegime)
            """, nativeQuery = true)
    List<TypeConvention> findAllByRegimeCode(@Param("codeRegime") String codeRegime);
}