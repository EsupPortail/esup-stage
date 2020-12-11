package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.NiveauFormation;
import org.springframework.data.repository.CrudRepository;

public interface NiveauFormationRepository extends CrudRepository<NiveauFormation, Integer> {
}
