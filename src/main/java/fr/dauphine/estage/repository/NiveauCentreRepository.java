package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.NiveauCentre;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class NiveauCentreRepository extends PaginationRepository<NiveauCentre> {

    public NiveauCentreRepository(EntityManager em) {
        super(em, NiveauCentre.class, "nc");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
