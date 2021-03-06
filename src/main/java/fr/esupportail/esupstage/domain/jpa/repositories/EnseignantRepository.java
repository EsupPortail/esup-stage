package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.Enseignant;

@Repository
public interface EnseignantRepository extends JpaRepository<Enseignant, Integer> {

	boolean existsOneByUidEnseignant(String uidEnseignant);

}
