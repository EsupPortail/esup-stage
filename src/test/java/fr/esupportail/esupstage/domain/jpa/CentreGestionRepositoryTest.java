package fr.esupportail.esupstage.domain.jpa;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.repositories.CentreGestionRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
class CentreGestionRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final CentreGestionRepository centreGestionRepository;

	private int centreGestionId;

	@Autowired
	CentreGestionRepositoryTest(final EntityManager entityManager, final CentreGestionRepository centreGestionRepository) {
		super();
		this.entityManager = entityManager;
		this.centreGestionRepository = centreGestionRepository;
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

		entityManager.persist(centreGestion);
		centreGestionId = centreGestion.getId();
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<CentreGestion> result = centreGestionRepository.findById(centreGestionId);
		assertTrue(result.isPresent(), "We should have found our CentreGestion");

		final CentreGestion centreGestion = result.get();
		assertTrue(centreGestion.isAutorisationEtudiantCreationConvention());
		assertEquals("codeuniv", centreGestion.getCodeUniversite());
		assertNotNull(centreGestion.getCreatedDate());
		assertEquals(1, centreGestion.getIdModeValidationStage());
		assertEquals("jdoe", centreGestion.getCreatedBy());
		assertEquals("libel", centreGestion.getConfidentialite().getLabel());
		assertEquals("libel", centreGestion.getNiveauCentre().getLabel());
	}

}
