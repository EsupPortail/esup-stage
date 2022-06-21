package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TemplateMailGroupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateMailGroupeJpaRepository extends JpaRepository<TemplateMailGroupe, Integer> {

    @Query("SELECT tm FROM TemplateMailGroupe tm WHERE tm.id = :id")
    TemplateMailGroupe findById(int id);

    @Query("SELECT tm FROM TemplateMailGroupe tm WHERE tm.code = :code")
    TemplateMailGroupe findByCode(String code);
}
