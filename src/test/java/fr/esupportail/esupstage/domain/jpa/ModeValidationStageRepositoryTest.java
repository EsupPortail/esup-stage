package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
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
import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import fr.esupportail.esupstage.domain.jpa.entities.Etudiant;
import fr.esupportail.esupstage.domain.jpa.entities.Indemnisation;
import fr.esupportail.esupstage.domain.jpa.entities.LangueConvention;
import fr.esupportail.esupstage.domain.jpa.entities.ModeValidationStage;
import fr.esupportail.esupstage.domain.jpa.entities.NatureTravail;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.entities.TempsTravail;
import fr.esupportail.esupstage.domain.jpa.entities.Theme;
import fr.esupportail.esupstage.domain.jpa.entities.TypeConvention;
import fr.esupportail.esupstage.domain.jpa.repositories.ModeValidationStageRepository;

@Rollback
@Transactional
class ModeValidationStageRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final ModeValidationStageRepository modeValidationStageRepository;

	private int lastInsertedId;

	@Autowired
	ModeValidationStageRepositoryTest(final EntityManager entityManager, final ModeValidationStageRepository modeValidationStageRepository) {
		super();
		this.entityManager = entityManager;
		this.modeValidationStageRepository = modeValidationStageRepository;
	}

	@BeforeEach
	void prepare() {
		ModeValidationStage modeValidationStage = new ModeValidationStage();
		modeValidationStage.setLibelleModeValidationStage("libelleModeValidationStage");
		modeValidationStage.setModifiable(true);
		modeValidationStage.setTemEnServModeValid("A");
		final Indemnisation indemnisation = new Indemnisation();
		indemnisation.setLibelleIndemnisation("libelleIndemnisation");
		indemnisation.setTemEnServIndem("A");

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

		TypeConvention typeConvention = new TypeConvention();
		typeConvention.setCodeCtrl("code");
		typeConvention.setLibelleTypeConvention("libel");
		typeConvention.setTemEnServTypeConvention("F");
		entityManager.persist(typeConvention);

		Theme theme = new Theme();
		theme.setLibelleTheme("libel");
		entityManager.persist(theme);

		TempsTravail tempsTravail = new TempsTravail();
		tempsTravail.setCodeCtrl("code");
		tempsTravail.setLibelleTempsTravail("libel");
		tempsTravail.setTemEnServTempsTravail("F");
		entityManager.persist(tempsTravail);

		NatureTravail natureTravail = new NatureTravail();
		natureTravail.setLibelleNatureTravail("libel");
		natureTravail.setTemEnServNatTrav("F");
		entityManager.persist(natureTravail);

		LangueConvention langueConvention = new LangueConvention();
		langueConvention.setCodeLangueConvention("CD");
		langueConvention.setLibelleLangueConvention("libel");
		entityManager.persist(langueConvention);

		Etudiant etudiant = new Etudiant();
		etudiant.setCodeUniversite("code");
		etudiant.setDateCreation(Calendar.getInstance().getTime());
		etudiant.setIdentEtudiant("ident");
		etudiant.setLoginCreation("login");
		etudiant.setNom("Name");
		etudiant.setNumEtudiant("125458");
		etudiant.setPrenom("Firstname");
		entityManager.persist(etudiant);

		Convention convention = new Convention();
		convention.setDateCreation(Calendar.getInstance().getTime());
		convention.setDateDebutStage(Calendar.getInstance().getTime());
		convention.setDateFinStage(Calendar.getInstance().getTime());
		convention.setDureeStage(100);
		convention.setIdAssurance(1);
		convention.setIdModeVersGratification(1);
		convention.setLoginCreation("login");
		convention.setNbJoursHebdo("1");
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

		modeValidationStage.setConventions(Arrays.asList(convention));
		entityManager.persist(modeValidationStage);

		this.entityManager.flush();

		this.entityManager.refresh(modeValidationStage);
		this.lastInsertedId = modeValidationStage.getIdModeValidationStage();
	}

	private void testModeCandidatureFields(int indice, ModeValidationStage modeValidationStage) {
		switch (indice) {
		case 0:
			assertEquals(this.lastInsertedId, modeValidationStage.getIdModeValidationStage(), "ModeValidationStage id match");
			assertEquals("libelleModeCandidature", modeValidationStage.getLibelleModeValidationStage(), "ModeValidationStage libelle match");
			assertEquals("A", modeValidationStage.getTemEnServModeValid(), "ModeValidationStage temEnServ match");
			assertEquals("subject", modeValidationStage.getConventions().get(0).getSujetStage(), "ModeValidationStage temEnServ match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<ModeValidationStage> result = this.modeValidationStageRepository.findById(1);
		assertTrue(result.isPresent(), "We should have found our ModeCandidature");

		final ModeValidationStage modeCandidature = result.get();
		this.testModeCandidatureFields(0, modeCandidature);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<ModeValidationStage> result = this.modeValidationStageRepository.findAll();
		assertTrue(result.size() == 1, "We should have found our Fichier");

		final ModeValidationStage modeCandidature = result.get(0);
		assertTrue(modeCandidature != null, "ModeCandidature exist");
		this.testModeCandidatureFields(0, modeCandidature);
	}

}
