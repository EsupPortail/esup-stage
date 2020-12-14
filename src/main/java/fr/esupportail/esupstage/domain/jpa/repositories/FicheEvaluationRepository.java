package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.FicheEvaluation;

@Repository
public interface FicheEvaluationRepository extends JpaRepository<FicheEvaluation, Integer> {
}
