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
import fr.esupportail.esupstage.domain.jpa.entities.Niveau;
import fr.esupportail.esupstage.domain.jpa.repositories.NiveauRepository;

@Rollback
@Transactional
class NiveauRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final NiveauRepository niveauRepository;

	private Integer lastInsertedId;

	@Autowired
	NiveauRepositoryTest(final EntityManager entityManager, final NiveauRepository niveauRepository) {
		super();
		this.entityManager = entityManager;
		this.niveauRepository = niveauRepository;
	}

	@BeforeEach
	void prepare() {
		final Niveau niveau = new Niveau();
		niveau.setValeur(1);

		this.entityManager.persist(niveau);
		this.entityManager.flush();

		this.entityManager.refresh(niveau);
		this.lastInsertedId = niveau.getId();
	}

	private void testNiveauFields(int indice, Niveau niveau) {
		switch (indice) {
		case 0:
			assertEquals(this.lastInsertedId, niveau.getId(), "Niveau id match");
			assertEquals(1, niveau.getValeur(), "Niveau valeur match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<Niveau> result = this.niveauRepository.findById(this.lastInsertedId);
		assertTrue(result.isPresent(), "We should have found our Niveau");

		final Niveau niveau = result.get();
		this.testNiveauFields(0, niveau);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<Niveau> result = this.niveauRepository.findAll();
		assertTrue(result.size() == 1, "We should have found our Niveau");

		final Niveau niveau = result.get(0);
		assertTrue(niveau != null, "Niveau exist");
		this.testNiveauFields(0, niveau);
	}

}
