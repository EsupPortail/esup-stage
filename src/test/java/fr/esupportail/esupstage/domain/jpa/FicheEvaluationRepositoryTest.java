package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import fr.esupportail.esupstage.AbstractTest;
import fr.esupportail.esupstage.domain.jpa.entities.CentreGestion;
import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;
import fr.esupportail.esupstage.domain.jpa.entities.FicheEvaluation;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.repositories.FicheEvaluationRepository;

@Rollback
@Transactional
class FicheEvaluationRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final FicheEvaluationRepository ficheEvaluationRepository;

	private int lastInsertedId;

	@Autowired
	FicheEvaluationRepositoryTest(final EntityManager entityManager, final FicheEvaluationRepository ficheEvaluationRepository) {
		super();
		this.entityManager = entityManager;
		this.ficheEvaluationRepository = ficheEvaluationRepository;
	}

	@BeforeEach
	void prepare() {
		final FicheEvaluation ficheEvaluation = new FicheEvaluation();

		final NiveauCentre niveauCentre = new NiveauCentre();
		niveauCentre.setLabel("libel");
		niveauCentre.setTemEnServ("A");
		entityManager.persist(niveauCentre);

		final Confidentialite confidentialite = new Confidentialite();
		confidentialite.setCode("A");
		confidentialite.setLabel("libel");
		confidentialite.setTemEnServ("A");
		entityManager.persist(confidentialite);

		final CentreGestion centreGestion = new CentreGestion();
		centreGestion.setAutorisationEtudiantCreationConvention(true);
		centreGestion.setCodeUniversite("codeuniv");
		centreGestion.setIdModeValidationStage(1);
		centreGestion.setCreatedBy("login");
		centreGestion.setConfidentialite(confidentialite);
		centreGestion.setNiveauCentre(niveauCentre);
		entityManager.persist(centreGestion);

		ficheEvaluation.setCentreGestion(centreGestion);
		entityManager.persist(centreGestion);

		this.entityManager.persist(ficheEvaluation);
		this.entityManager.flush();
		this.entityManager.refresh(ficheEvaluation);
		this.lastInsertedId = ficheEvaluation.getId();
	}

	private void testFicheEvaluationFields(int indice, FicheEvaluation ficheEvaluation) {
		switch (indice) {
		case 0:
			assertEquals(this.lastInsertedId, ficheEvaluation.getId(), "FicheEvaluation id match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<FicheEvaluation> result = this.ficheEvaluationRepository.findById(this.lastInsertedId);
		assertTrue(result.isPresent(), "We should have found our FicherEvaluation");

		final FicheEvaluation fapQualification = result.get();
		this.testFicheEvaluationFields(0, fapQualification);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<FicheEvaluation> result = this.ficheEvaluationRepository.findAll();
		assertEquals(1, result.size(), "We should have found our FicherEvaluation");

		final FicheEvaluation fapQualification = result.get(0);
		assertNotNull(fapQualification, "FicherEvaluation exist");
		this.testFicheEvaluationFields(0, fapQualification);
	}

}
