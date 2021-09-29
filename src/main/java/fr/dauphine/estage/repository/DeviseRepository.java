package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Devise;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class DeviseRepository extends PaginationRepository<Devise> {

    public DeviseRepository(EntityManager em) {
        super(em, Devise.class, "d");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
