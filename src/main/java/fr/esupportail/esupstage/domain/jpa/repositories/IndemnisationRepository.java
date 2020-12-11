package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.Indemnisation;
import org.springframework.data.repository.CrudRepository;

public interface IndemnisationRepository extends CrudRepository<Indemnisation, Integer> {
}
