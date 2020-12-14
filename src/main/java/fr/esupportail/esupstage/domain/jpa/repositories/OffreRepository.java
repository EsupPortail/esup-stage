package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Offre;

public interface OffreRepository extends JpaRepository<Offre, Integer> {
}
