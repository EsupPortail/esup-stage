package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.TypeStructure;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class TypeStructureRepository extends PaginationRepository<TypeStructure> {

    public TypeStructureRepository(EntityManager em) {
        super(em, TypeStructure.class, "ts");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
