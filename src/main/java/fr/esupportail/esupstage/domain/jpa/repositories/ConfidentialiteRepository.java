package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;

public interface ConfidentialiteRepository extends JpaRepository<Confidentialite, String> {
}
