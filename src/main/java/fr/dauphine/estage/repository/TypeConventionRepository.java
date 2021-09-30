package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.TypeConvention;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class TypeConventionRepository extends PaginationRepository<TypeConvention> {

    public TypeConventionRepository(EntityManager em) {
        super(em, TypeConvention.class, "tc");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
