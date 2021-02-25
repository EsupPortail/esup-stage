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
import fr.esupportail.esupstage.domain.jpa.entities.Affectation;
import fr.esupportail.esupstage.domain.jpa.entities.AffectationPK;
import fr.esupportail.esupstage.domain.jpa.entities.CentreGestion;
import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;
import fr.esupportail.esupstage.domain.jpa.entities.DroitAdministration;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.entities.PersonnelCentreGestion;
import fr.esupportail.esupstage.domain.jpa.repositories.PersonnelCentreGestionRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
public class PersonnelCentreGestionRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final PersonnelCentreGestionRepository repository;

	private Integer id;

	@Autowired
	PersonnelCentreGestionRepositoryTest(final EntityManager entityManager, final PersonnelCentreGestionRepository personnelCentreGestionRepository) {
		super();
		this.entityManager = entityManager;
		repository = personnelCentreGestionRepository;
	}

	@BeforeEach
	void prepare() {
		final AffectationPK affectationPK = new AffectationPK();
		affectationPK.setCodeAffectation("CodeAffect");
		affectationPK.setCodeUniversite("CodeUniv");

		final Affectation affectation = new Affectation();
		affectation.setId(affectationPK);
		affectation.setLibelleAffectation("Label");
		entityManager.persist(affectation);

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

		final DroitAdministration droitAdministration = new DroitAdministration();
		droitAdministration.setLabel("Label");
		droitAdministration.setTemEnServ("A");
		entityManager.persist(droitAdministration);

		final PersonnelCentreGestion entity = new PersonnelCentreGestion();
		entity.setImpressionConvention(true);
		entity.setNom("Doe");
		entity.setPrenom("John");
		entity.setUidPersonnel("jdoe");
		entity.setAffectation(affectation);
		entity.setCentreGestion(centreGestion);
		entity.setDroitAdministration(droitAdministration);

		entityManager.persist(entity);
		entityManager.flush();

		entityManager.refresh(entity);
		id = entity.getId();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<PersonnelCentreGestion> result = repository.findById(id);
		assertTrue(result.isPresent(), "We should have found our entity");

		final PersonnelCentreGestion tmp = result.get();
		assertTrue(tmp.isImpressionConvention());
		assertEquals("John", tmp.getPrenom());
		assertEquals("Doe", tmp.getNom());
		assertEquals("jdoe", tmp.getUidPersonnel());
		assertEquals("jdoe", tmp.getCreatedBy());
	}

}
