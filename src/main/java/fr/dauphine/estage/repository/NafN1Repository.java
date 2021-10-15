package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.NafN1;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class NafN1Repository extends PaginationRepository<NafN1> {

    public NafN1Repository(EntityManager em) {
        super(em, NafN1.class, "n");
        this.predicateWhitelist = Arrays.asList("code", "libelle");
    }
}
