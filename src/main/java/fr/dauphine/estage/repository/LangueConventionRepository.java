package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.LangueConvention;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

@Repository
public class LangueConventionRepository extends PaginationRepository<LangueConvention> {

    public LangueConventionRepository(EntityManager em) {
        super(em, LangueConvention.class, "lc");
        this.predicateWhitelist = Arrays.asList("code", "libelle");
    }

    public boolean exists(LangueConvention langueConvention) {
        String queryString = "SELECT code FROM LangueConvention WHERE code = :code";
        TypedQuery<String> query = em.createQuery(queryString, String.class);
        query.setParameter("code", langueConvention.getCode());
        List<String> results = query.getResultList();
        return results.size() > 0;
    }
}
