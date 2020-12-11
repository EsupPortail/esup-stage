package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.Assurance;
import org.springframework.data.repository.CrudRepository;

public interface AssuranceRepository extends CrudRepository<Assurance, Integer> {
}
