package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;
import org.springframework.data.repository.CrudRepository;

public interface ConfidentialiteRepository extends CrudRepository<Confidentialite, String> {
}
