package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.Fichier;
import org.springframework.data.repository.CrudRepository;

public interface FichierRepository extends CrudRepository<Fichier, Integer> {
}
