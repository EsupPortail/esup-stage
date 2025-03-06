package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.NiveauFormation;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class NiveauFormationRepository extends PaginationRepository<NiveauFormation> {

    public NiveauFormationRepository(EntityManager em) {
        super(em, NiveauFormation.class, "nf");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
