package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.esup_portail.esup_stage.model.Role;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class RoleRepository extends PaginationRepository<Role> {

    public RoleRepository(EntityManager em) {
        super(em, Role.class, "r");
        this.predicateWhitelist = Arrays.asList("code", "libelle");
    }

    public boolean exist(Role role) {
        TypedQuery<Integer> query = em.createQuery("SELECT id FROM Role WHERE code = :code OR libelle = :libelle", Integer.class);
        query.setParameter("code", role.getCode());
        query.setParameter("libelle", role.getLibelle());
        List<Integer> results = query.getResultList();
        if (role.getId() == 0 && results.size() > 0) {
            return true;
        }
        return results.stream().anyMatch(i -> i != role.getId());
    }
}
