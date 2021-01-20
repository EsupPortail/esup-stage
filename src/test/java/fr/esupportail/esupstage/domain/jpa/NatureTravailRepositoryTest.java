package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import fr.esupportail.esupstage.domain.jpa.entities.NatureTravail;
import fr.esupportail.esupstage.domain.jpa.repositories.NatureTravailRepository;

@Rollback
@Transactional
class NatureTravailRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final NatureTravailRepository natureTravailRepository;

	private Integer lastInsertedId;

	@Autowired
	NatureTravailRepositoryTest(final EntityManager entityManager, final NatureTravailRepository natureTravailRepository) {
		super();
		this.entityManager = entityManager;
		this.natureTravailRepository = natureTravailRepository;
	}

	@BeforeEach
	void prepare() {
		final NatureTravail natureTravail = new NatureTravail();

		natureTravail.setLibelleNatureTravail("libelleNatureTravail");
		natureTravail.setTemEnServNatTrav("A");

		this.entityManager.persist(natureTravail);
		this.entityManager.flush();

		this.entityManager.refresh(natureTravail);

		this.lastInsertedId = natureTravail.getId();
	}

	private void testNatureTravailFields(int indice, NatureTravail natureTravail) {
		switch (indice) {
		case 0:
			assertEquals(this.lastInsertedId, natureTravail.getId(), "NatureTravail id match");
			assertEquals("libelleNatureTravail", natureTravail.getLibelleNatureTravail(), "NatureTravail libelle match");
			assertEquals("A", natureTravail.getTemEnServNatTrav(), "NatureTravail TemEnServ match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<NatureTravail> result = this.natureTravailRepository.findById(this.lastInsertedId);
		assertTrue(result.isPresent(), "We should have found our NatureTravail");

		final NatureTravail natureTravail = result.get();
		this.testNatureTravailFields(0, natureTravail);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<NatureTravail> result = this.natureTravailRepository.findAll();
		assertTrue(result.size() == 1, "We should have found our NatureTravail");

		final NatureTravail natureTravail = result.get(0);
		assertTrue(natureTravail != null, "NatureTravail exist");
		this.testNatureTravailFields(0, natureTravail);
	}

}
