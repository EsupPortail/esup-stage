package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TemplateConvention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateConventionJpaRepository extends JpaRepository<TemplateConvention, Integer> {

    TemplateConvention findById(int id);

    @Query("SELECT tc FROM TemplateConvention tc where tc.typeConvention.id = :id AND tc.langueConvention.code = :code")
    TemplateConvention findByTypeAndLangue(@Param("id") int id, @Param("code") String code);

    @Query("SELECT COUNT(tc.id) FROM TemplateConvention tc WHERE tc.typeConvention.id = :idTypeConvention")
    Long countTemplateWithTypeConvention(@Param("idTypeConvention") int idTypeConvention);
}
