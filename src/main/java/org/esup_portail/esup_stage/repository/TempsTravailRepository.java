package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TempsTravail;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class TempsTravailRepository extends PaginationRepository<TempsTravail> {

    public TempsTravailRepository(EntityManager em) {
        super(em, TempsTravail.class, "tt");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
