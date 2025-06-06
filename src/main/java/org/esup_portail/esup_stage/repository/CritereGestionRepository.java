package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.CritereGestion;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class CritereGestionRepository extends PaginationRepository<CritereGestion> {

    public CritereGestionRepository(EntityManager em) {
        super(em, CritereGestion.class, "cg");
        this.predicateWhitelist = Arrays.asList("id.code", "id.codeVersionEtape", "libelle");
    }
}
