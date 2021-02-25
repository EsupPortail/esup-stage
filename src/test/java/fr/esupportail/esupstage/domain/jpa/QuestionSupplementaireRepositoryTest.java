package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;

import fr.esupportail.esupstage.AbstractTest;
import fr.esupportail.esupstage.domain.jpa.entities.CentreGestion;
import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;
import fr.esupportail.esupstage.domain.jpa.entities.FicheEvaluation;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.entities.QuestionSupplementaire;
import fr.esupportail.esupstage.domain.jpa.repositories.QuestionSupplementaireRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
public class QuestionSupplementaireRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final QuestionSupplementaireRepository repository;

	private Integer id;

	@Autowired
	QuestionSupplementaireRepositoryTest(final EntityManager entityManager, final QuestionSupplementaireRepository repository) {
		super();
		this.entityManager = entityManager;
		this.repository = repository;
	}

	@BeforeEach
	void prepare() {
		final NiveauCentre niveauCentre = new NiveauCentre();
		niveauCentre.setLabel("Label");
		niveauCentre.setTemEnServ("A");
		entityManager.persist(niveauCentre);

		final Confidentialite confidentialite = new Confidentialite();
		confidentialite.setCode("A");
		confidentialite.setLabel("Label");
		confidentialite.setTemEnServ("A");
		entityManager.persist(confidentialite);

		final CentreGestion centreGestion = new CentreGestion();
		centreGestion.setAutorisationEtudiantCreationConvention(true);
		centreGestion.setCodeUniversite("CodeUniv");
		centreGestion.setIdModeValidationStage(1);
		centreGestion.setConfidentialite(confidentialite);
		centreGestion.setNiveauCentre(niveauCentre);
		entityManager.persist(centreGestion);

		final FicheEvaluation ficheEvaluation = new FicheEvaluation();
		ficheEvaluation.setCentreGestion(centreGestion);
		entityManager.persist(ficheEvaluation);

		final QuestionSupplementaire questionSupplementaire = new QuestionSupplementaire();
		questionSupplementaire.setIdPlacement(1);
		questionSupplementaire.setQuestion("Question");
		questionSupplementaire.setTypeQuestion("Type");
		questionSupplementaire.setFicheEvaluation(ficheEvaluation);
		entityManager.persist(questionSupplementaire);

		entityManager.flush();

		entityManager.refresh(questionSupplementaire);
		id = questionSupplementaire.getId();
	}

	@Test
	@DisplayName("findById – Nominal Test Case")
	void findById() {
		final Optional<QuestionSupplementaire> result = repository.findById(id);
		assertTrue(result.isPresent(), "We should have found our entity");

		final QuestionSupplementaire tmp = result.get();
		assertEquals(1, tmp.getIdPlacement());
		assertEquals("Question", tmp.getQuestion());
		assertEquals("Type", tmp.getTypeQuestion());
	}

}
