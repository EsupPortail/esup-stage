package fr.esupportail.esupstage.domain.jpa;

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
import fr.esupportail.esupstage.domain.jpa.entities.CritereGestion;
import fr.esupportail.esupstage.domain.jpa.entities.CritereGestionPK;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.repositories.CritereGestionRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
class CritereGestionRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final CritereGestionRepository critereRepository;

	private CritereGestionPK critereGestionId;

	@Autowired
	CritereGestionRepositoryTest(final EntityManager entityManager, final CritereGestionRepository critereGestionRepository) {
		super();
		this.entityManager = entityManager;
		critereRepository = critereGestionRepository;
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

		final CritereGestionPK critereGestionPK = new CritereGestionPK();
		critereGestionPK.setCodeCritere("code");
		critereGestionPK.setCodeVersionEtape("code");

		final CritereGestion critereGestion = new CritereGestion();
		critereGestion.setId(critereGestionPK);
		critereGestion.setLibelleCritere("libel");
		critereGestion.setCentreGestion(centreGestion);
		entityManager.persist(critereGestion);

		critereGestionId = critereGestion.getId();
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<CritereGestion> result = critereRepository.findById(critereGestionId);
		assertTrue(result.isPresent(), "We should have found our CritereGestion");

		final CritereGestion critereGestion = result.get();
		assertEquals("libel", critereGestion.getLibelleCritere());
		assertEquals("jdoe", critereGestion.getCentreGestion().getCreatedBy());
	}

}