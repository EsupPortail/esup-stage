package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.TicketStructure;

public interface TicketStructureRepository extends JpaRepository<TicketStructure, String> {
}
