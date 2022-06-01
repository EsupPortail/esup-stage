package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.EtudiantGroupeEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtudiantGroupeEtudiantJpaRepository extends JpaRepository<EtudiantGroupeEtudiant, Integer> {

}