package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.Categorie;
import org.springframework.data.repository.CrudRepository;

public interface CategorieRepository extends CrudRepository<Categorie, Integer> {
}
