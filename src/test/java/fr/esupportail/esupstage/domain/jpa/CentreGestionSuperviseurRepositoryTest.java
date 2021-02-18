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
import fr.esupportail.esupstage.domain.jpa.entities.CentreGestionSuperViseur;
import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.repositories.CentreGestionSuperViseurRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
class CentreGestionSuperviseurRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final CentreGestionSuperViseurRepository centreGestionSuperViseurRepository;

	private int centreGestionSuperviseurId;

	@Autowired
	CentreGestionSuperviseurRepositoryTest(final EntityManager entityManager, final CentreGestionSuperViseurRepository centreGestionSuperViseurRepository) {
		super();
		this.entityManager = entityManager;
		this.centreGestionSuperViseurRepository = centreGestionSuperViseurRepository;
	}

	@BeforeEach
	void prepare() {

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
		centreGestion.setIdModeValidationStage(1);
		centreGestion.setConfidentialite(confidentialite);
		centreGestion.setNiveauCentre(niveauCentre);
		entityManager.persist(centreGestion);

		final CentreGestionSuperViseur centreGestionSuperViseur = new CentreGestionSuperViseur();
		centreGestionSuperViseur.setNomCentreSuperViseur("name");
		centreGestionSuperViseur.addCentreGestion(centreGestion);
		entityManager.persist(centreGestionSuperViseur);

		centreGestionSuperviseurId = centreGestionSuperViseur.getIdCentreGestionSuperViseur();
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<CentreGestionSuperViseur> result = centreGestionSuperViseurRepository.findById(centreGestionSuperviseurId);
		assertTrue(result.isPresent(), "We should have found our CentreGestionSuperviseur");

		final CentreGestionSuperViseur centreGestionSuperViseur = result.get();
		assertEquals("codeuniv", centreGestionSuperViseur.getCentreGestions().get(0).getCodeUniversite());
		assertEquals("name", centreGestionSuperViseur.getNomCentreSuperViseur());
	}

}
