package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.Effectif;
import org.springframework.data.repository.CrudRepository;

public interface EffectifRepository extends CrudRepository<Effectif, Integer> {
}
