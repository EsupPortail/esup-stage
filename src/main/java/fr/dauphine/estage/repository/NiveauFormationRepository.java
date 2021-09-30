package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.NiveauFormation;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class NiveauFormationRepository extends PaginationRepository<NiveauFormation> {

    public NiveauFormationRepository(EntityManager em) {
        super(em, NiveauFormation.class, "nf");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
