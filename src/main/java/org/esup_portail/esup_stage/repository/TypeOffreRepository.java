package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TypeOffre;
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
