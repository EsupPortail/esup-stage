package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.ContratOffre;

@Repository
public interface ContratOffreRepository extends JpaRepository<ContratOffre, Integer> {
}
