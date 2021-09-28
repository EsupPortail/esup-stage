package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Pays;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class PaysRepository extends PaginationRepository<Pays> {

    public PaysRepository(EntityManager em) {
        super(em, Pays.class, "p");
        this.predicateWhitelist = Arrays.asList("id", "lib");
    }
}
