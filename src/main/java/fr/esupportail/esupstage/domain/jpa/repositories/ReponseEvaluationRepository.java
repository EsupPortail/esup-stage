package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.ReponseEvaluation;
import fr.esupportail.esupstage.domain.jpa.entities.ReponseEvaluationPK;

@Repository
public interface ReponseEvaluationRepository extends JpaRepository<ReponseEvaluation, ReponseEvaluationPK> {
}
