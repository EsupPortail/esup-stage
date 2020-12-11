package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.ReponseSupplementaire;
import fr.esupportail.esupstage.domain.jpa.entities.ReponseSupplementairePK;
import org.springframework.data.repository.CrudRepository;

public interface ReponseSupplementaireRepository extends CrudRepository<ReponseSupplementaire, ReponseSupplementairePK> {
}
