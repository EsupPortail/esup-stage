package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import fr.esupportail.esupstage.domain.jpa.entities.NiveauFormation;
import fr.esupportail.esupstage.domain.jpa.repositories.NiveauFormationRepository;

@Rollback
@Transactional
class NiveauFormationRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final NiveauFormationRepository niveauFormationRepository;

	private Integer lastInsertedId;

	@Autowired
	NiveauFormationRepositoryTest(final EntityManager entityManager, final NiveauFormationRepository niveauFormationRepository) {
		super();
		this.entityManager = entityManager;
		this.niveauFormationRepository = niveauFormationRepository;
	}

	@BeforeEach
	void prepare() {
		final NiveauFormation niveauFormation = new NiveauFormation();
		niveauFormation.setLabel("libelleNiveauFormation");
		niveauFormation.setModifiable(true);
		niveauFormation.setTemEnServ("A");

		this.entityManager.persist(niveauFormation);
		this.entityManager.flush();

		this.entityManager.refresh(niveauFormation);
		this.lastInsertedId = niveauFormation.getId();
	}

	private void testNiveauFormationFields(int indice, NiveauFormation niveauFormation) {
		switch (indice) {
		case 0:
			assertEquals(this.lastInsertedId, niveauFormation.getId(), "NiveauFormation libelle match");
			assertEquals("libelleNiveauFormation", niveauFormation.getLabel(), "NiveauFormation libelle match");
			assertEquals("A", niveauFormation.getTemEnServ(), "NiveauFormation TemEnServ match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<NiveauFormation> result = this.niveauFormationRepository.findById(this.lastInsertedId);
		assertTrue(result.isPresent(), "We should have found our NiveauFormation");

		final NiveauFormation niveauFormation = result.get();
		this.testNiveauFormationFields(0, niveauFormation);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<NiveauFormation> result = this.niveauFormationRepository.findAll();
		assertEquals(1, result.size(), "We should have found our NiveauFormation");

		final NiveauFormation niveauFormation = result.get(0);
		assertNotNull(niveauFormation, "NiveauFormation exist");
		this.testNiveauFormationFields(0, niveauFormation);
	}

}
