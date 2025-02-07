package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TemplateConvention;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

import java.util.Arrays;

@Repository
public class TemplateConventionRepository extends PaginationRepository<TemplateConvention> {

    public TemplateConventionRepository(EntityManager em) {
        super(em, TemplateConvention.class, "tc");
        this.predicateWhitelist = Arrays.asList("typeConvention.libelle", "langueConvention.code");
    }
}
