package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.ContratOffre;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Arrays;
import java.util.List;

@Repository
public class ContratOffreRepository extends PaginationRepository<ContratOffre> {

    public ContratOffreRepository(EntityManager em) {
        super(em, ContratOffre.class, "co");
        this.predicateWhitelist = Arrays.asList("id", "libelle", "codeCtrl");
    }

    public boolean exists(String codeCtrl, int id) {
        String queryString = "SELECT id FROM " + this.typeClass.getName() + " WHERE codeCtrl = :codeCtrl";
        TypedQuery<Integer> query = em.createQuery(queryString, Integer.class);
        query.setParameter("codeCtrl", codeCtrl);
        List<Integer> results = query.getResultList();
        if (id == 0 && results.size() > 0) {
            return true;
        }

        return results.stream().anyMatch(i -> i != id);
    }
}
