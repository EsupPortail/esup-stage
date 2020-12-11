package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.Ufr;
import fr.esupportail.esupstage.domain.jpa.entities.UfrPK;
import org.springframework.data.repository.CrudRepository;

public interface UfrRepository extends CrudRepository<Ufr, UfrPK> {
}
