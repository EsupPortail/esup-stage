package fr.esupportail.esupstage.domain.jpa.repositories;


import fr.esupportail.esupstage.domain.jpa.entities.Etape;
import fr.esupportail.esupstage.domain.jpa.entities.EtapePK;
import org.springframework.data.repository.CrudRepository;

public interface EtapeRepository extends CrudRepository<Etape, EtapePK> {
}
