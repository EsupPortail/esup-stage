package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.TicketStructure;
import org.springframework.data.repository.CrudRepository;

public interface TicketStructureRepository extends CrudRepository<TicketStructure, String> {
}
