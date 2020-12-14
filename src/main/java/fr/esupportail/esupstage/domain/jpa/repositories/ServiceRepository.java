package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Service;

public interface ServiceRepository extends JpaRepository<Service, Integer> {
}
