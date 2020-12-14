package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Assurance;

public interface AssuranceRepository extends JpaRepository<Assurance, Integer> {
}
