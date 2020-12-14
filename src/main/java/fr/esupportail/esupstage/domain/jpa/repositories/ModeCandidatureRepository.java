package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.ModeCandidature;

@Repository
public interface ModeCandidatureRepository extends JpaRepository<ModeCandidature, Integer> {
}
