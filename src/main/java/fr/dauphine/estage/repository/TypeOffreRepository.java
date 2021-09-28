package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.TypeOffre;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class TypeOffreRepository extends PaginationRepository<TypeOffre> {

    public TypeOffreRepository(EntityManager em) {
        super(em, TypeOffre.class, "to");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
