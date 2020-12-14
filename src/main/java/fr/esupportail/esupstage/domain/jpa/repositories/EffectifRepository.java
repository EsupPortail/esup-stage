package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.Effectif;

@Repository
public interface EffectifRepository extends JpaRepository<Effectif, Integer> {
}
