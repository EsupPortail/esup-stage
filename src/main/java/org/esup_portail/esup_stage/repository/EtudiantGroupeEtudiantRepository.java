package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.EtudiantGroupeEtudiant;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class EtudiantGroupeEtudiantRepository extends PaginationRepository<EtudiantGroupeEtudiant> {

    public EtudiantGroupeEtudiantRepository(EntityManager em) {super(em, EtudiantGroupeEtudiant.class, "ege");}
}