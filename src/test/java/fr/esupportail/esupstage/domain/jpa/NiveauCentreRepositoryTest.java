package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
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
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.repositories.NiveauCentreRepository;

@Rollback
@Transactional
class NiveauCentreRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final NiveauCentreRepository niveauCentreRepository;

	private Integer lastInsertedId;

	@Autowired
	NiveauCentreRepositoryTest(final EntityManager entityManager, final NiveauCentreRepository niveauCentreRepository) {
		super();
		this.entityManager = entityManager;
		this.niveauCentreRepository = niveauCentreRepository;
	}

	@BeforeEach
	void prepare() {
		final NiveauCentre niveauCentre = new NiveauCentre();
		niveauCentre.setLabel("libelleNiveauCentre");
		niveauCentre.setTemEnServ("A");

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
		this.entityManager.persist(niveauCentre);

		entityManager.persist(centreGestion);

		niveauCentre.setCentreGestions(Arrays.asList(centreGestion));
		this.entityManager.persist(niveauCentre);

		this.entityManager.flush();

		this.entityManager.refresh(niveauCentre);
		this.lastInsertedId = niveauCentre.getId();
	}

	private void testNiveauCentreFields(int indice, NiveauCentre niveauCentre) {
		switch (indice) {
		case 0:
			assertEquals(this.lastInsertedId, niveauCentre.getId(), "NiveauCentre libelle match");
			assertEquals("libelleNiveauCentre", niveauCentre.getLabel(), "NiveauCentre libelle match");
			assertEquals("A", niveauCentre.getTemEnServ(), "NiveauCentre TemEnServ match");
			assertEquals(1, niveauCentre.getCentreGestions().size(), "NiveauCentre.CentreGestions size match");
			assertEquals("codeuniv", niveauCentre.getCentreGestions().get(0).getCodeUniversite(), "NiveauCentre.CentreGestion codeuniv match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<NiveauCentre> result = this.niveauCentreRepository.findById(this.lastInsertedId);
		assertTrue(result.isPresent(), "We should have found our NiveauCentre");

		final NiveauCentre niveauCentre = result.get();
		this.testNiveauCentreFields(0, niveauCentre);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<NiveauCentre> result = this.niveauCentreRepository.findAll();
		assertEquals(1, result.size(), "We should have found our NiveauCentre");

		final NiveauCentre niveauCentre = result.get(0);
		assertNotNull(niveauCentre, "NiveauCentre exist");
		this.testNiveauCentreFields(0, niveauCentre);
	}

}
