package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.ReponseEvaluation;
import fr.esupportail.esupstage.domain.jpa.entities.ReponseEvaluationPK;

public interface ReponseEvaluationRepository extends JpaRepository<ReponseEvaluation, ReponseEvaluationPK> {
}
