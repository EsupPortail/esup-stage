package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.OffreDiffusion;
import fr.esupportail.esupstage.domain.jpa.entities.OffreDiffusionPK;
import org.springframework.data.repository.CrudRepository;

public interface OffreDiffusionRepository extends CrudRepository<OffreDiffusion, OffreDiffusionPK> {
}
