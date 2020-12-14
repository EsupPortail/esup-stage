package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.Affectation;
import fr.esupportail.esupstage.domain.jpa.entities.AffectationPK;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, AffectationPK> {
}
