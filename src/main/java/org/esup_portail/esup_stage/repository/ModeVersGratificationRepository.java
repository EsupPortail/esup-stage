package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.ModeVersGratification;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class ModeVersGratificationRepository extends PaginationRepository<ModeVersGratification> {

    public ModeVersGratificationRepository(EntityManager em) {
        super(em, ModeVersGratification.class, "mg");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
