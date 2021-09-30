package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.LangueConvention;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class LangueConventionRepository extends PaginationRepository<LangueConvention> {

    public LangueConventionRepository(EntityManager em) {
        super(em, LangueConvention.class, "lc");
        this.predicateWhitelist = Arrays.asList("code", "libelle");
    }
}
