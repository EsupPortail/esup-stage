package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TemplateConvention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateConventionJpaRepository extends JpaRepository<TemplateConvention, Integer> {

    @Query("""
        SELECT tc FROM TemplateConvention tc
        JOIN tc.typeConventions ttc
        WHERE ttc.id = :id AND tc.langueConvention.code = :code
    """)
    TemplateConvention findByTypeAndLangue(@Param("id") int id, @Param("code") String code);

    @Query("""
        SELECT COUNT(DISTINCT tc.id) FROM TemplateConvention tc
        JOIN tc.typeConventions ttc
        WHERE ttc.id = :idTypeConvention
    """)
    Long countTemplateWithTypeConvention(@Param("idTypeConvention") int idTypeConvention);
}
