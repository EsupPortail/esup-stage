package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Enseignant;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

@Repository
public class EnseignantRepository extends PaginationRepository<Enseignant> {

    public EnseignantRepository(EntityManager em) {
        super(em, Enseignant.class, "e");
    }
}