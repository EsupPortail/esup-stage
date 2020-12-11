package fr.esupportail.esupstage.domain.jpa.repositories;

import java.util.List;

import fr.esupportail.esupstage.domain.jpa.entities.Pays;
import org.springframework.data.repository.CrudRepository;

public interface PaysRepository extends CrudRepository<Pays, Integer> {
	
	public List<Pays> findByIso2ContainingIgnoreCase(String iso2);
}
