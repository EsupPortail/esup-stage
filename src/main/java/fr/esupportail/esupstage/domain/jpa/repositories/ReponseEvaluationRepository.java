package fr.esupportail.esupstage.domain.jpa.repositories;

import fr.esupportail.esupstage.domain.jpa.entities.ReponseEvaluation;
import fr.esupportail.esupstage.domain.jpa.entities.ReponseEvaluationPK;
import org.springframework.data.repository.CrudRepository;

public interface ReponseEvaluationRepository extends CrudRepository<ReponseEvaluation, ReponseEvaluationPK> {
}
