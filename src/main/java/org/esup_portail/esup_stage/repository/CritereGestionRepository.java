package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.CritereGestion;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class CritereGestionRepository extends PaginationRepository<CritereGestion> {

    public CritereGestionRepository(EntityManager em) {
        super(em, CritereGestion.class, "cg");
        this.predicateWhitelist = Arrays.asList("id.code", "id.codeVersionEtape", "libelle");
    }
}
