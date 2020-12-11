package fr.esupportail.esupstage.domain.jpa.repositories;


import fr.esupportail.esupstage.domain.jpa.entities.CritereGestion;
import fr.esupportail.esupstage.domain.jpa.entities.CritereGestionPK;
import org.springframework.data.repository.CrudRepository;

public interface CritereGestionRepository extends CrudRepository<CritereGestion, CritereGestionPK> {
}
