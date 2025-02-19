package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.esup_portail.esup_stage.model.Etudiant;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EtudiantRepository extends PaginationRepository<Etudiant> {

    public EtudiantRepository(EntityManager em) {
        super(em, Etudiant.class, "e");
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
