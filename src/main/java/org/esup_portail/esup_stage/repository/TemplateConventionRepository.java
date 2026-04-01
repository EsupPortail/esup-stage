package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.TemplateConvention;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TemplateConventionRepository extends PaginationRepository<TemplateConvention> {

    public TemplateConventionRepository(EntityManager em) {
        super(em, TemplateConvention.class, "tc");
        this.predicateWhitelist = List.of("langueConvention.code","libelle");
    }
}