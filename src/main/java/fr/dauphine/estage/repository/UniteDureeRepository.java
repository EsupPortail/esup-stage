package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.UniteDuree;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class UniteDureeRepository extends PaginationRepository<UniteDuree> {

    public UniteDureeRepository(EntityManager em) {
        super(em, UniteDuree.class, "ud");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
