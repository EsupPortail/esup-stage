package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Enseignant;

public interface EnseignantRepository extends JpaRepository<Enseignant, Integer> {
}
