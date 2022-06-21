package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.GroupeEtudiant;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class GroupeEtudiantRepository extends PaginationRepository<GroupeEtudiant> {

    public GroupeEtudiantRepository(EntityManager em) {
        super(em, GroupeEtudiant.class, "e");
   }

}
