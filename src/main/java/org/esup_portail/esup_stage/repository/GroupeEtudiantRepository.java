package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.GroupeEtudiant;
import org.esup_portail.esup_stage.model.LangueConvention;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Repository
public class GroupeEtudiantRepository extends PaginationRepository<GroupeEtudiant> {

    public GroupeEtudiantRepository(EntityManager em) {
        super(em, GroupeEtudiant.class, "e");
   }

   @Override
    public boolean exists(String code, int id) {
        String queryString = "SELECT id FROM " + this.typeClass.getName() + " WHERE code = :code";
        TypedQuery<Integer> query = em.createQuery(queryString, Integer.class);
        query.setParameter("code", code);
        List<Integer> results = query.getResultList();
        if (id == 0 && results.size() > 0) {
            return true;
        }

        return results.stream().anyMatch(i -> i != id);
    }
}
