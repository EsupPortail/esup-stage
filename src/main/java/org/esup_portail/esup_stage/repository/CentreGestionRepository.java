package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.CentreGestion;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class CentreGestionRepository extends PaginationRepository<CentreGestion> {

    public CentreGestionRepository(EntityManager em) {
        super(em, CentreGestion.class, "cg");
        this.predicateWhitelist = Arrays.asList("id", "nomCentre", "niveauCentre.libelle");
    }
}
