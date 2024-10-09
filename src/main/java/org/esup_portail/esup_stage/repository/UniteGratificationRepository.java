package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.UniteGratification;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class UniteGratificationRepository extends PaginationRepository<UniteGratification> {

    public UniteGratificationRepository(EntityManager em) {
        super(em, UniteGratification.class, "ug");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
