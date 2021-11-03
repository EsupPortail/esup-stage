package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.CentreGestion;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

@Repository
public class CentreGestionRepository extends PaginationRepository<CentreGestion> {

    public CentreGestionRepository(EntityManager em) {
        super(em, CentreGestion.class, "cg");
        this.predicateWhitelist = Arrays.asList("id", "nomCentre", "niveauCentre.libelle");
    }

    public boolean etablissementExists() {
        String queryString = "SELECT cg.id FROM CentreGestion cg WHERE cg.niveauCentre.libelle = 'ETABLISSEMENT'";
        TypedQuery<Integer> query = em.createQuery(queryString, Integer.class);
        List<Integer> results = query.getResultList();
        return results.size() > 0;
    }
}
