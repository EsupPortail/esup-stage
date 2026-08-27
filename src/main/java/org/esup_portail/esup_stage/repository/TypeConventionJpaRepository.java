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

    /**
     * Types de convention proposables pour un régime d'inscription : actifs, associés au régime
     * et disposant d'au moins un modèle d'impression.
     */
    @Query("""
            SELECT DISTINCT tc
            FROM TypeConvention tc
            JOIN tc.regimesInscription ri
            JOIN tc.templates template
            WHERE LOWER(ri.code) = LOWER(:codeRegimeInscription)
              AND tc.temEnServ = 'O'
            ORDER BY tc.libelle
            """)
    List<TypeConvention> findAllActiveByCodeRegimeInscription(@Param("codeRegimeInscription") String codeRegimeInscription);
}
