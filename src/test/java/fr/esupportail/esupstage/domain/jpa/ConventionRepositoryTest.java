package fr.esupportail.esupstage.domain.jpa;

import static org.junit.Assert.assertNotNull;
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
import fr.esupportail.esupstage.domain.jpa.entities.Indemnisation;
import fr.esupportail.esupstage.domain.jpa.entities.LangueConvention;
import fr.esupportail.esupstage.domain.jpa.entities.ModeValidationStage;
import fr.esupportail.esupstage.domain.jpa.entities.NatureTravail;
import fr.esupportail.esupstage.domain.jpa.entities.NbJourHebdo;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.entities.TempsTravail;
import fr.esupportail.esupstage.domain.jpa.entities.Theme;
import fr.esupportail.esupstage.domain.jpa.entities.TypeConvention;
import fr.esupportail.esupstage.domain.jpa.repositories.ConventionRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
class ConventionRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final ConventionRepository conventionRepository;

	private int conventionId;

	@Autowired
	ConventionRepositoryTest(final EntityManager entityManager, final ConventionRepository conventionRepository) {
		super();
		this.entityManager = entityManager;
		this.conventionRepository = conventionRepository;
	}

	@BeforeEach
	void prepare() {

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
		centreGestion.setConfidentialite(confidentialite);
		centreGestion.setNiveauCentre(niveauCentre);
		entityManager.persist(centreGestion);

		final TypeConvention typeConvention = new TypeConvention();
		typeConvention.setCodeCtrl("code");
		typeConvention.setLabel("libel");
		typeConvention.setTemEnServ("F");
		entityManager.persist(typeConvention);

		final Theme theme = new Theme();
		theme.setLabel("libel");
		entityManager.persist(theme);

		final TempsTravail tempsTravail = new TempsTravail();
		tempsTravail.setCodeCtrl("code");
		tempsTravail.setLabel("libel");
		tempsTravail.setTemEnServ("F");
		entityManager.persist(tempsTravail);

		final NatureTravail natureTravail = new NatureTravail();
		natureTravail.setLabel("libel");
		natureTravail.setTemEnServ("F");
		entityManager.persist(natureTravail);

		final ModeValidationStage modeValidationStage = new ModeValidationStage();
		modeValidationStage.setLabel("libel");
		modeValidationStage.setTemEnServ("F");
		entityManager.persist(modeValidationStage);

		final LangueConvention langueConvention = new LangueConvention();
		langueConvention.setCode("CD");
		langueConvention.setLabel("libel");
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

		conventionId = convention.getId();
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<Convention> result = conventionRepository.findById(conventionId);
		assertTrue(result.isPresent(), "We should have found our Convention");

		final Convention convention = result.get();
		assertNotNull(convention.getCreatedDate());
		assertEquals(LocalDate.of(2020,1, 1), convention.getDateDebutStage());
		assertEquals(LocalDate.of(2020,1, 20), convention.getDateFinStage());
		assertEquals(100, convention.getDureeStage());
		assertEquals(1, convention.getIdModeVersGratification());
		assertEquals(1, convention.getIdAssurance());
		assertEquals("jdoe", convention.getCreatedBy());
		assertEquals(NbJourHebdo.NB_JOURS_1_0, convention.getNbJoursHebdo());
		assertEquals("subject", convention.getSujetStage());
		assertEquals("s", convention.getTemConfSujetTeme());
		assertEquals("125458", convention.getEtudiant().getNumEtudiant());
		assertEquals("indem", convention.getIndemnisation().getLabel());
		assertEquals("CD", convention.getLangueConvention().getCode());
		assertEquals("libel", convention.getModeValidationStage().getLabel());
		assertEquals("libel", convention.getNatureTravail().getLabel());
		assertEquals("libel", convention.getTempsTravail().getLabel());
		assertEquals("libel", convention.getTheme().getLabel());
		assertEquals("libel", convention.getTypeConvention().getLabel());
		assertEquals("jdoe", convention.getCentreGestion().getCreatedBy());
	}

}
