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
import fr.esupportail.esupstage.domain.jpa.entities.Enseignant;
import fr.esupportail.esupstage.domain.jpa.repositories.EnseignantRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
public class EnseignantRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final EnseignantRepository repository;

	private Integer idEnseignant;

	@Autowired
	EnseignantRepositoryTest(final EntityManager entityManager, final EnseignantRepository repository) {
		super();
		this.entityManager = entityManager;
		this.repository = repository;
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

		final Enseignant enseignant = new Enseignant();
		enseignant.setUidEnseignant("jdoe");
		enseignant.setPrenom("John");
		enseignant.setNom("Doe");
		enseignant.setCodeUniversite("ESUP");
		enseignant.setAffectation(affectation);

		entityManager.persist(enseignant);
		entityManager.flush();

		entityManager.refresh(enseignant);
		idEnseignant = enseignant.getId();
	}

	@Test
	@DisplayName("")
	void test() {
		final Optional<Enseignant> result = repository.findById(idEnseignant);
		assertTrue(result.isPresent());

		final Enseignant enseignant = result.get();
		assertEquals("John", enseignant.getPrenom());
		assertEquals("Doe", enseignant.getNom());
	}

	@Test
	@DisplayName("existsOneByUidEnseignant – Nominal test case")
	void existsOneByUidEnseignant01() {
		final boolean result = repository.existsOneByUidEnseignant("jdoe");
		assertTrue(result, "We should have found our teacher");
	}

}
