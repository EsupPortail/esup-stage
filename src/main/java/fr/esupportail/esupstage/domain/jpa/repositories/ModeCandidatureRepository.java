package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.ModeCandidature;
import org.springframework.data.repository.CrudRepository;

public interface ModeCandidatureRepository extends CrudRepository<ModeCandidature, Integer> {
}
