package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
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
import fr.esupportail.esupstage.domain.jpa.entities.Fichier;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.repositories.FichierRepository;

@Rollback
@Transactional
class FichierRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final FichierRepository fichierRepository;

	private int lastInsertedId;

	@Autowired
	FichierRepositoryTest(final EntityManager entityManager, final FichierRepository fichierRepository) {
		super();
		this.entityManager = entityManager;
		this.fichierRepository = fichierRepository;
	}

	@BeforeEach
	void prepare() {
		final Fichier fichier = new Fichier();
		fichier.setNomFichier("nomFichier");
		fichier.setNomReel("nomReel");

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

		fichier.setCentreGestions(Arrays.asList(centreGestion));

		this.entityManager.persist(fichier);
		this.entityManager.flush();

		this.entityManager.refresh(fichier);
		this.lastInsertedId = fichier.getId();
	}

	private void testFicheEvaluationFields(int indice, Fichier fichier) {
		switch (indice) {
		case 0:
			assertEquals(this.lastInsertedId, fichier.getId(), "Fichier id match");
			assertEquals("nomFichier", fichier.getNomFichier(), "Fichier name match");
			assertEquals("nomReel", fichier.getNomReel(), "Fichier real name match");
			assertEquals("1", fichier.getCentreGestions().size(), "Fichier.CentreGestion size match");
			assertEquals("codeuniv", fichier.getCentreGestions().get(0).getCodeUniversite(), "Fichier.CentreGestion codeUniversite match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<Fichier> result = this.fichierRepository.findById(this.lastInsertedId);
		assertTrue(result.isPresent(), "We should have found our Fichier");

		final Fichier fichier = result.get();
		this.testFicheEvaluationFields(0, fichier);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<Fichier> result = this.fichierRepository.findAll();
		assertTrue(result.size() == 1, "We should have found our Fichier");

		final Fichier fichier = result.get(0);
		assertTrue(fichier != null, "Fichier exist");
		this.testFicheEvaluationFields(0, fichier);
	}

}
