package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Avenant;

public interface AvenantRepository extends JpaRepository<Avenant, Integer> {
}
