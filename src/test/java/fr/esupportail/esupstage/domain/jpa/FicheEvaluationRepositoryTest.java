package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import fr.esupportail.esupstage.domain.jpa.entities.Effectif;
import fr.esupportail.esupstage.domain.jpa.entities.FAP_Qualification;
import fr.esupportail.esupstage.domain.jpa.entities.FAP_QualificationSimplifiee;
import fr.esupportail.esupstage.domain.jpa.entities.FicheEvaluation;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.entities.Offre;
import fr.esupportail.esupstage.domain.jpa.entities.ReponseEvaluation;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.entities.TypeOffre;
import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.FAP_QualificationSimplifieeRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.FicheEvaluationRepository;

@Rollback
@Transactional
class FicheEvaluationRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final FicheEvaluationRepository ficheEvaluationRepository;

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

		this.entityManager.persist(ficheEvaluation);
		this.entityManager.flush();
	}

	private void testFicheEvaluationFields(int indice, FicheEvaluation ficheEvaluation) {
		switch (indice) {
		case 0:
			assertEquals(1, ficheEvaluation.getIdFicheEvaluation(), "FicheEvaluation id match");
			assertEquals("codeuniv", ficheEvaluation.getCentreGestion().getCodeUniversite(), "FicheEvaluation.CentreGestion codeUniversite match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<FicheEvaluation> result = this.ficheEvaluationRepository.findById(1);
		assertTrue(result.isPresent(), "We should have found our FAP_QualificationSimplifiee");

		final FicheEvaluation fapQualification = result.get();
		this.testFicheEvaluationFields(0, fapQualification);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<FicheEvaluation> result = this.ficheEvaluationRepository.findAll();
		assertTrue(result.size() == 1, "We should have found our FAP_QualificationSimplifiee");

		final FicheEvaluation fapQualification = result.get(0);
		assertTrue(fapQualification != null, "FAP_QualificationSimplifiee exist");
		this.testFicheEvaluationFields(0, fapQualification);
	}

}
