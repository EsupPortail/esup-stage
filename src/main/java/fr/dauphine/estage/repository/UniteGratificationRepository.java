package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.UniteGratification;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class UniteGratificationRepository extends PaginationRepository<UniteGratification> {

    public UniteGratificationRepository(EntityManager em) {
        super(em, UniteGratification.class, "ug");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
