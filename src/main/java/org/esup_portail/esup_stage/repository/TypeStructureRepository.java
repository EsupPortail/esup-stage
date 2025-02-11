package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TypeStructure;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

import java.util.Arrays;

@Repository
public class TypeStructureRepository extends PaginationRepository<TypeStructure> {

    public TypeStructureRepository(EntityManager em) {
        super(em, TypeStructure.class, "ts");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
