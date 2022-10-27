package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Affectation;
import org.esup_portail.esup_stage.model.Etudiant;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class EtudiantRepository {
    protected final EntityManager em;

    public EtudiantRepository(EntityManager em) {
        this.em = em;
    }

    public Etudiant findByNumEtudiant(String numEtudiant) {
        TypedQuery<Etudiant> query = em.createQuery("SELECT e FROM Etudiant e WHERE e.numEtudiant = :numEtudiant ORDER BY e.identEtudiant DESC", Etudiant.class);
        query.setParameter("numEtudiant", numEtudiant);
        query.setMaxResults(1);
        List<Etudiant> results = query.getResultList();
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }
}
