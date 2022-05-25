package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Etudiant;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class EtudiantRepository extends PaginationRepository<Etudiant> {

    public EtudiantRepository(EntityManager em) {
        super(em, Etudiant.class, "e");
   }

}
