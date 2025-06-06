package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.TypeStructure;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class TypeStructureRepository extends PaginationRepository<TypeStructure> {

    public TypeStructureRepository(EntityManager em) {
        super(em, TypeStructure.class, "ts");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
