package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.NiveauFormation;

public interface NiveauFormationRepository extends JpaRepository<NiveauFormation, Integer> {
}
