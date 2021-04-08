package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import fr.esupportail.esupstage.domain.jpa.entities.NbJourHebdo;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.entities.TempsTravail;
import fr.esupportail.esupstage.domain.jpa.entities.Theme;
import fr.esupportail.esupstage.domain.jpa.entities.TypeConvention;
import fr.esupportail.esupstage.domain.jpa.repositories.LangueConventionRepository;

@Rollback
@Transactional
class LangueConventionRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final LangueConventionRepository langueConventionRepository;

	@Autowired
	LangueConventionRepositoryTest(final EntityManager entityManager, final LangueConventionRepository langueConventionRepository) {
		super();
		this.entityManager = entityManager;
		this.langueConventionRepository = langueConventionRepository;
	}

	@BeforeEach
	void prepare() {
		LangueConvention langueConvention = new LangueConvention();
		langueConvention.setCode("CD");
		langueConvention.setLabel("libel");
		langueConvention.setTemEnServ("A");

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

		TypeConvention typeConvention = new TypeConvention();
		typeConvention.setCodeCtrl("code");
		typeConvention.setLabel("libel");
		typeConvention.setTemEnServ("F");
		entityManager.persist(typeConvention);

		Theme theme = new Theme();
		theme.setLabel("libel");
		entityManager.persist(theme);

		TempsTravail tempsTravail = new TempsTravail();
		tempsTravail.setCodeCtrl("code");
		tempsTravail.setLabel("libel");
		tempsTravail.setTemEnServ("F");
		entityManager.persist(tempsTravail);

		NatureTravail natureTravail = new NatureTravail();
		natureTravail.setLabel("libel");
		natureTravail.setTemEnServ("F");
		entityManager.persist(natureTravail);

		ModeValidationStage modeValidationStage = new ModeValidationStage();
		modeValidationStage.setLabel("libel");
		modeValidationStage.setTemEnServ("F");
		entityManager.persist(modeValidationStage);

		Etudiant etudiant = new Etudiant();
		etudiant.setCodeUniversite("code");
		etudiant.setCreatedDate(LocalDateTime.now());
		etudiant.setIdentEtudiant("ident");
		etudiant.setCreatedBy("login");
		etudiant.setNom("Name");
		etudiant.setNumEtudiant("125458");
		etudiant.setPrenom("Firstname");
		entityManager.persist(etudiant);

		final Indemnisation indemnisation = new Indemnisation();
		indemnisation.setLabel("libelleIndemnisation");
		indemnisation.setTemEnServ("A");
		entityManager.persist(indemnisation);

		entityManager.persist(langueConvention);

		Convention convention = new Convention();
		convention.setDateDebutStage(LocalDate.now());
		convention.setDateFinStage(LocalDate.now());
		convention.setDureeStage(100);
		convention.setIdAssurance(1);
		convention.setIdModeVersGratification(1);
		convention.setNbJoursHebdo(NbJourHebdo.NB_JOURS_1_0);
		convention.setCreatedBy("root");
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

		this.entityManager.flush();

		this.entityManager.refresh(langueConvention);
	}

	private void testLangueConventionFields(int indice, LangueConvention langueConvention) {
		switch (indice) {
		case 0:
			assertEquals("CD", langueConvention.getCode(), "LangueConvention code match");
			assertEquals("libel", langueConvention.getLabel(), "LangueConvention libelle match");
			assertEquals("A", langueConvention.getTemEnServ(), "LangueConvention temEnServLangue match");
			assertEquals("subject", langueConvention.getConventions().get(0).getSujetStage(), "LangueConvention.Conventions subject match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<LangueConvention> result = this.langueConventionRepository.findById("CD");
		assertTrue(result.isPresent(), "We should have found our LangueConvention");

		final LangueConvention indemnisation = result.get();
		this.testLangueConventionFields(0, indemnisation);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<LangueConvention> result = this.langueConventionRepository.findAll();
		assertEquals(1, result.size(), "We should have found our LangueConvention");

		final LangueConvention indemnisation = result.get(0);
		assertNotNull(indemnisation, "Fichier exist");
		this.testLangueConventionFields(0, indemnisation);
	}

}
