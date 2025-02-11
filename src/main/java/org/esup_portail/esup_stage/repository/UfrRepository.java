package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Ufr;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

import java.util.Arrays;

@Repository
public class UfrRepository extends PaginationRepository<Ufr> {

    public UfrRepository(EntityManager em) {
        super(em, Ufr.class, "ufr");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
