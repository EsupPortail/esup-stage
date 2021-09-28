package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.StatutJuridique;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class StatutJuridiqueRepository extends PaginationRepository<StatutJuridique> {

    public StatutJuridiqueRepository(EntityManager em) {
        super(em, StatutJuridique.class, "sj");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
