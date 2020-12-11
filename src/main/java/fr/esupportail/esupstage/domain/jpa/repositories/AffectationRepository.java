package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.Affectation;
import fr.esupportail.esupstage.domain.jpa.entities.AffectationPK;
import org.springframework.data.repository.CrudRepository;

public interface AffectationRepository extends CrudRepository<Affectation, AffectationPK> {
}
