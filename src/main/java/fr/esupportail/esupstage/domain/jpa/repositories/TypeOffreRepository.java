package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.TypeOffre;

@Repository
public interface TypeOffreRepository extends JpaRepository<TypeOffre, Integer> {
}
