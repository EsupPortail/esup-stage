package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.NafN1;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

import java.util.Arrays;

@Repository
public class NafN1Repository extends PaginationRepository<NafN1> {

    public NafN1Repository(EntityManager em) {
        super(em, NafN1.class, "n");
        this.predicateWhitelist = Arrays.asList("code", "libelle");
    }
}
