package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TemplateMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateMailJpaRepository extends JpaRepository<TemplateMail, Integer> {

    @Query("SELECT tm FROM TemplateMail tm WHERE tm.id = :id")
    TemplateMail findById(int id);

    @Query("SELECT tm FROM TemplateMail tm WHERE tm.code = :code")
    TemplateMail findByCode(String code);
}
