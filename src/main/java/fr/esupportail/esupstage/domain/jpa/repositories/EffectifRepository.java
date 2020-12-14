package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Effectif;

public interface EffectifRepository extends JpaRepository<Effectif, Integer> {
}
