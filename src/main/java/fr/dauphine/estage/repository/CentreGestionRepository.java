package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.CentreGestion;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class CentreGestionRepository extends PaginationRepository<CentreGestion> {

    public CentreGestionRepository(EntityManager em) {
        super(em, CentreGestion.class, "cg");
        this.predicateWhitelist = Arrays.asList("id", "nomCentre");
    }
}
