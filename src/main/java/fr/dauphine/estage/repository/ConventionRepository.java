package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Convention;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class ConventionRepository extends PaginationRepository<Convention> {

    public ConventionRepository(EntityManager em) {
        super(em, Convention.class, "c");
        this.predicateWhitelist = Arrays.asList("id");
    }
}
