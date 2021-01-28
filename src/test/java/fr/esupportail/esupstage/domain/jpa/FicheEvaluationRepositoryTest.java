package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
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
		niveauCentre.setLibelleNiveauCentre("libel");
		niveauCentre.setTemEnServNiveauCentre("A");
		entityManager.persist(niveauCentre);

		final Confidentialite confidentialite = new Confidentialite();
		confidentialite.setCodeConfidentialite("A");
		confidentialite.setLibelleConfidentialite("libel");
		confidentialite.setTemEnServConfid("A");
		entityManager.persist(confidentialite);

		final CentreGestion centreGestion = new CentreGestion();
		centreGestion.setAutorisationEtudiantCreationConvention(true);
		centreGestion.setCodeUniversite("codeuniv");
		centreGestion.setDateCreation(Calendar.getInstance().getTime());
		centreGestion.setIdModeValidationStage(1);
		centreGestion.setLoginCreation("login");
		centreGestion.setConfidentialite(confidentialite);
		centreGestion.setNiveauCentre(niveauCentre);
		entityManager.persist(centreGestion);

		ficheEvaluation.setCentreGestion(centreGestion);
		entityManager.persist(centreGestion);

		this.entityManager.persist(ficheEvaluation);
		this.entityManager.flush();
		this.entityManager.refresh(ficheEvaluation);
		this.lastInsertedId = ficheEvaluation.getIdFicheEvaluation();
	}

	private void testFicheEvaluationFields(int indice, FicheEvaluation ficheEvaluation) {
		switch (indice) {
		case 0:
			assertEquals(this.lastInsertedId, ficheEvaluation.getIdFicheEvaluation(), "FicheEvaluation id match");
			assertEquals("codeuniv", ficheEvaluation.getCentreGestion().getCodeUniversite(), "FicheEvaluation.CentreGestion codeUniversite match");
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
		assertTrue(result.size() == 1, "We should have found our FicherEvaluation");

		final FicheEvaluation fapQualification = result.get(0);
		assertTrue(fapQualification != null, "FicherEvaluation exist");
		this.testFicheEvaluationFields(0, fapQualification);
	}

}
