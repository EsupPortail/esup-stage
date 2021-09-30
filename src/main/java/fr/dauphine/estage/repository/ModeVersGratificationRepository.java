package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.ModeVersGratification;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class ModeVersGratificationRepository extends PaginationRepository<ModeVersGratification> {

    public ModeVersGratificationRepository(EntityManager em) {
        super(em, ModeVersGratification.class, "mg");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
