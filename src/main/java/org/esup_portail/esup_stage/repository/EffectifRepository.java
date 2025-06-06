package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.Effectif;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class EffectifRepository extends PaginationRepository<Effectif> {

    public EffectifRepository(EntityManager em) {
        super(em, Effectif.class, "e");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
