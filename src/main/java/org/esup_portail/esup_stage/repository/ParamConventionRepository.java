package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.ParamConvention;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class ParamConventionRepository extends PaginationRepository<ParamConvention> {

    public ParamConventionRepository(EntityManager em) {
        super(em, ParamConvention.class, "pc");
        this.predicateWhitelist = Arrays.asList("code", "libelle");
    }
}
