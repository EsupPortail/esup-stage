package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Fichier;

public interface FichierRepository extends JpaRepository<Fichier, Integer> {
}
