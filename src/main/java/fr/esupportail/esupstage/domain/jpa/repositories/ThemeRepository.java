package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.Theme;
import org.springframework.data.repository.CrudRepository;

public interface ThemeRepository extends CrudRepository<Theme, Integer> {
}
