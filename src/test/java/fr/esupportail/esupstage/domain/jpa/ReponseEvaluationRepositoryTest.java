package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
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
import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import fr.esupportail.esupstage.domain.jpa.entities.Etudiant;
import fr.esupportail.esupstage.domain.jpa.entities.FicheEvaluation;
import fr.esupportail.esupstage.domain.jpa.entities.Indemnisation;
import fr.esupportail.esupstage.domain.jpa.entities.LangueConvention;
import fr.esupportail.esupstage.domain.jpa.entities.ModeValidationStage;
import fr.esupportail.esupstage.domain.jpa.entities.NatureTravail;
import fr.esupportail.esupstage.domain.jpa.entities.NbJourHebdo;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.entities.ReponseEvaluation;
import fr.esupportail.esupstage.domain.jpa.entities.ReponseEvaluationPK;
import fr.esupportail.esupstage.domain.jpa.entities.TempsTravail;
import fr.esupportail.esupstage.domain.jpa.entities.Theme;
import fr.esupportail.esupstage.domain.jpa.entities.TypeConvention;
import fr.esupportail.esupstage.domain.jpa.repositories.ReponseEvaluationRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
public class ReponseEvaluationRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final ReponseEvaluationRepository repository;

	private ReponseEvaluationPK id;

	@Autowired
	ReponseEvaluationRepositoryTest(final EntityManager entityManager, final ReponseEvaluationRepository repository) {
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

		final TypeConvention typeConvention = new TypeConvention();
		typeConvention.setCodeCtrl("code");
		typeConvention.setLabel("Label");
		typeConvention.setTemEnServ("F");
		entityManager.persist(typeConvention);

		final Theme theme = new Theme();
		theme.setLabel("Label");
		entityManager.persist(theme);

		final TempsTravail tempsTravail = new TempsTravail();
		tempsTravail.setCodeCtrl("code");
		tempsTravail.setLabel("Label");
		tempsTravail.setTemEnServ("F");
		entityManager.persist(tempsTravail);

		final NatureTravail natureTravail = new NatureTravail();
		natureTravail.setLabel("Label");
		natureTravail.setTemEnServ("F");
		entityManager.persist(natureTravail);

		final ModeValidationStage modeValidationStage = new ModeValidationStage();
		modeValidationStage.setLabel("Label");
		modeValidationStage.setTemEnServ("F");
		entityManager.persist(modeValidationStage);

		final LangueConvention langueConvention = new LangueConvention();
		langueConvention.setCode("CD");
		langueConvention.setLabel("Label");
		entityManager.persist(langueConvention);

		final Indemnisation indemnisation = new Indemnisation();
		indemnisation.setLabel("indem");
		indemnisation.setTemEnServ("F");
		entityManager.persist(indemnisation);

		final Etudiant etudiant = new Etudiant();
		etudiant.setCodeUniversite("code");
		etudiant.setIdentEtudiant("ident");
		etudiant.setNom("Name");
		etudiant.setNumEtudiant("125458");
		etudiant.setPrenom("Firstname");
		entityManager.persist(etudiant);

		final Convention convention = new Convention();
		convention.setDateDebutStage(LocalDate.of(2020, 1, 1));
		convention.setDateFinStage(LocalDate.of(2020, 1, 20));
		convention.setDureeStage(100);
		convention.setIdAssurance(1);
		convention.setIdModeVersGratification(1);
		convention.setNbJoursHebdo(NbJourHebdo.NB_JOURS_1_0);
		convention.setSujetStage("subject");
		convention.setTemConfSujetTeme("s");
		convention.setEtudiant(etudiant);
		convention.setIndemnisation(indemnisation);
		convention.setLangueConvention(langueConvention);
		convention.setModeValidationStage(modeValidationStage);
		convention.setNatureTravail(natureTravail);
		convention.setTempsTravail(tempsTravail);
		convention.setTheme(theme);
		convention.setTypeConvention(typeConvention);
		convention.setCentreGestion(centreGestion);
		entityManager.persist(convention);
		entityManager.flush();

		entityManager.refresh(ficheEvaluation);
		entityManager.refresh(convention);

		final ReponseEvaluation reponseEvaluation = new ReponseEvaluation();
		id = new ReponseEvaluationPK(ficheEvaluation.getId(), convention.getId());
		reponseEvaluation.setId(id);
		reponseEvaluation.setReponseEnt1(666);
		reponseEvaluation.setImpressionEtudiant(true);
		reponseEvaluation.setReponseEnsI3("Value");
		entityManager.persist(reponseEvaluation);
		entityManager.flush();
	}

	@Test
	@DisplayName("findById ??? Nominal Test Case")
	void findById() {
		final Optional<ReponseEvaluation> result = repository.findById(id);
		assertTrue(result.isPresent(), "We should have found our entity");

		final ReponseEvaluation tmp = result.get();
		assertEquals(666, tmp.getReponseEnt1());
		assertEquals("Value", tmp.getReponseEnsI3());
		assertTrue(tmp.isImpressionEtudiant());
	}

}
