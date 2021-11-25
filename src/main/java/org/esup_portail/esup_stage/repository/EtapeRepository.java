package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Etape;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class EtapeRepository extends PaginationRepository<Etape> {

    public EtapeRepository(EntityManager em) {
        super(em, Etape.class, "e");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
