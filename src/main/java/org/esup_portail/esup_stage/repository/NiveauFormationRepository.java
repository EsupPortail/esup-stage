package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.NiveauFormation;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

import java.util.Arrays;

@Repository
public class NiveauFormationRepository extends PaginationRepository<NiveauFormation> {

    public NiveauFormationRepository(EntityManager em) {
        super(em, NiveauFormation.class, "nf");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
