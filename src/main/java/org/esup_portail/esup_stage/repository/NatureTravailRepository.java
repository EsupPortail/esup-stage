package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.NatureTravail;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

import java.util.Arrays;

@Repository
public class NatureTravailRepository extends PaginationRepository<NatureTravail> {

    public NatureTravailRepository(EntityManager em) {
        super(em, NatureTravail.class, "mv");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}