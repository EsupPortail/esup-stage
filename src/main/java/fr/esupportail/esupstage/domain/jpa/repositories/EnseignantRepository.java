package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.Enseignant;
import org.springframework.data.repository.CrudRepository;

public interface EnseignantRepository extends CrudRepository<Enseignant, Integer> {
}
