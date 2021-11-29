package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Fichier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichierJpaRepository extends JpaRepository<Fichier, Integer> {

    Fichier findById(int id);
}
