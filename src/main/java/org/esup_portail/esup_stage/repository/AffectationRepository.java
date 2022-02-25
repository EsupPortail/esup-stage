package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Affectation;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Repository
public class AffectationRepository {

    protected final EntityManager em;

    public AffectationRepository(EntityManager em) {
        this.em = em;
    }

    public Affectation getOneNotNullCodeUniversite() {
        TypedQuery<Affectation> query = em.createQuery("SELECT a FROM Affectation a WHERE a.id.codeUniversite != ''", Affectation.class);
        query.setMaxResults(1);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
