package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Pays;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

@Repository
public class PaysRepository extends PaginationRepository<Pays> {

    public PaysRepository(EntityManager em) {
        super(em, Pays.class, "p");
        this.predicateWhitelist = Arrays.asList("id", "lib");
    }

    public boolean exists(Pays pays) {
        String queryString = "SELECT id FROM Pays WHERE lib = :lib";
        TypedQuery<Integer> query = em.createQuery(queryString, Integer.class);
        query.setParameter("lib", pays.getLib());
        List<Integer> results = query.getResultList();
        if (pays.getId() == 0 && results.size() > 0) {
            return true;
        }

        return results.stream().anyMatch(i -> i != pays.getId());
    }
}
