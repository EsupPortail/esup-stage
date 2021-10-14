package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Pays;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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
