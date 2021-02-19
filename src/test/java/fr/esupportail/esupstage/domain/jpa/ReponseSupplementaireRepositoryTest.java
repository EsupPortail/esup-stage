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
import fr.esupportail.esupstage.domain.jpa.entities.QuestionSupplementaire;
import fr.esupportail.esupstage.domain.jpa.entities.ReponseSupplementaire;
import fr.esupportail.esupstage.domain.jpa.entities.ReponseSupplementairePK;
import fr.esupportail.esupstage.domain.jpa.entities.TempsTravail;
import fr.esupportail.esupstage.domain.jpa.entities.Theme;
import fr.esupportail.esupstage.domain.jpa.entities.TypeConvention;
import fr.esupportail.esupstage.domain.jpa.repositories.ReponseSupplementaireRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
public class ReponseSupplementaireRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final ReponseSupplementaireRepository repository;

	private ReponseSupplementairePK id;

	@Autowired
	ReponseSupplementaireRepositoryTest(final EntityManager entityManager, final ReponseSupplementaireRepository repository) {
		super();
		this.entityManager = entityManager;
		this.repository = repository;
	}

	@BeforeEach
	void prepare() {
		final NiveauCentre niveauCentre = new NiveauCentre();
		niveauCentre.setLibelleNiveauCentre("Label");
		niveauCentre.setTemEnServNiveauCentre("A");
		entityManager.persist(niveauCentre);

		final Confidentialite confidentialite = new Confidentialite();
		confidentialite.setCodeConfidentialite("A");
		confidentialite.setLibelleConfidentialite("Label");
		confidentialite.setTemEnServConfid("A");
		entityManager.persist(confidentialite);

		final CentreGestion centreGestion = new CentreGestion();
		centreGestion.setAutorisationEtudiantCreationConvention(true);
		centreGestion.setCodeUniversite("CodeUniv");
		centreGestion.setIdModeValidationStage(1);
		centreGestion.setConfidentialite(confidentialite);
		centreGestion.setNiveauCentre(niveauCentre);
		entityManager.persist(centreGestion);

		final TypeConvention typeConvention = new TypeConvention();
		typeConvention.setCodeCtrl("code");
		typeConvention.setLibelleTypeConvention("Label");
		typeConvention.setTemEnServTypeConvention("F");
		entityManager.persist(typeConvention);

		final Theme theme = new Theme();
		theme.setLibelleTheme("Label");
		entityManager.persist(theme);

		final TempsTravail tempsTravail = new TempsTravail();
		tempsTravail.setCodeCtrl("code");
		tempsTravail.setLibelleTempsTravail("Label");
		tempsTravail.setTemEnServTempsTravail("F");
		entityManager.persist(tempsTravail);

		final NatureTravail natureTravail = new NatureTravail();
		natureTravail.setLibelleNatureTravail("Label");
		natureTravail.setTemEnServNatTrav("F");
		entityManager.persist(natureTravail);

		final ModeValidationStage modeValidationStage = new ModeValidationStage();
		modeValidationStage.setLibelleModeValidationStage("Label");
		modeValidationStage.setTemEnServModeValid("F");
		entityManager.persist(modeValidationStage);

		final LangueConvention langueConvention = new LangueConvention();
		langueConvention.setCodeLangueConvention("CD");
		langueConvention.setLibelleLangueConvention("Label");
		entityManager.persist(langueConvention);

		final Indemnisation indemnisation = new Indemnisation();
		indemnisation.setLibelleIndemnisation("indem");
		indemnisation.setTemEnServIndem("F");
		entityManager.persist(indemnisation);

		final Etudiant etudiant = new Etudiant();
		etudiant.setCodeUniversite("code");
		etudiant.setIdentEtudiant("ident");
		etudiant.setNom("Name");
		etudiant.setNumEtudiant("125458");
		etudiant.setPrenom("Firstname");
		entityManager.persist(etudiant);

		final FicheEvaluation ficheEvaluation = new FicheEvaluation();
		ficheEvaluation.setCentreGestion(centreGestion);
		entityManager.persist(ficheEvaluation);

		final Convention convention = new Convention();
		convention.setDateDebutStage(LocalDate.of(2020,1, 1));
		convention.setDateFinStage(LocalDate.of(2020,1, 20));
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

		final QuestionSupplementaire questionSupplementaire = new QuestionSupplementaire();
		questionSupplementaire.setIdPlacement(1);
		questionSupplementaire.setQuestion("Question");
		questionSupplementaire.setTypeQuestion("Type");
		questionSupplementaire.setFicheEvaluation(ficheEvaluation);
		entityManager.persist(questionSupplementaire);

		entityManager.flush();

		entityManager.refresh(convention);
		entityManager.refresh(questionSupplementaire);

		id = new ReponseSupplementairePK(questionSupplementaire.getId(), convention.getIdConvention());
		final ReponseSupplementaire reponseSupplementaire = new ReponseSupplementaire();
		reponseSupplementaire.setId(id);
		reponseSupplementaire.setReponseTxt("Txt");
		reponseSupplementaire.setReponseBool(true);
		reponseSupplementaire.setReponseInt(666);
		entityManager.persist(reponseSupplementaire);
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal Test Case")
	void findById() {
		final Optional<ReponseSupplementaire> result = repository.findById(id);
		assertTrue(result.isPresent(), "We should have found our entity");

		final ReponseSupplementaire tmp = result.get();
		assertTrue(tmp.isReponseBool());
		assertEquals("Txt", tmp.getReponseTxt());
		assertEquals(666, tmp.getReponseInt());
	}

}
