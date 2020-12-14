package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.ModeCandidature;

public interface ModeCandidatureRepository extends JpaRepository<ModeCandidature, Integer> {
}
