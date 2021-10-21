package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.ContratOffre;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class ContratOffreRepository extends PaginationRepository<ContratOffre> {

    public ContratOffreRepository(EntityManager em) {
        super(em, ContratOffre.class, "co");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
