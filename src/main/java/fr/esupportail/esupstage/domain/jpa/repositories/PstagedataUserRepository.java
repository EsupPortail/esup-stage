package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.PstagedataUser;
import org.springframework.data.repository.CrudRepository;

public interface PstagedataUserRepository extends CrudRepository<PstagedataUser, String> {
}
