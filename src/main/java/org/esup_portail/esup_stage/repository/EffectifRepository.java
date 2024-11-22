package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Effectif;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class EffectifRepository extends PaginationRepository<Effectif> {

    public EffectifRepository(EntityManager em) {
        super(em, Effectif.class, "e");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
