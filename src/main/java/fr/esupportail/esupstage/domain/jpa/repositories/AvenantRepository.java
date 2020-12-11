package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.Avenant;
import org.springframework.data.repository.CrudRepository;

public interface AvenantRepository extends CrudRepository<Avenant, Integer> {
}
