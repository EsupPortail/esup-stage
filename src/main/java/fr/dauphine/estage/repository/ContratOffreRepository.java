package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.ContratOffre;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class ContratOffreRepository extends PaginationRepository<ContratOffre> {

    public ContratOffreRepository(EntityManager em) {
        super(em, ContratOffre.class, "co");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
