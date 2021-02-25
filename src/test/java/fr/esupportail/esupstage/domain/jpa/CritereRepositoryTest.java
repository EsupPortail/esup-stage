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
import org.springframework.test.annotation.Rollback;

import fr.esupportail.esupstage.AbstractTest;
import fr.esupportail.esupstage.domain.jpa.entities.Categorie;
import fr.esupportail.esupstage.domain.jpa.entities.Critere;
import fr.esupportail.esupstage.domain.jpa.entities.Niveau;
import fr.esupportail.esupstage.domain.jpa.repositories.CritereRepository;

@Rollback
@Transactional
class CritereRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final CritereRepository critereRepository;

	private int critereId;

	@Autowired
	CritereRepositoryTest(final EntityManager entityManager, final CritereRepository critereRepository) {
		super();
		this.entityManager = entityManager;
		this.critereRepository = critereRepository;
	}

	@BeforeEach
	void prepare() {

		final Niveau niveau = new Niveau();
		niveau.setValue(1);
		entityManager.persist(niveau);

		final Categorie categorie = new Categorie();
		categorie.setType(1);
		entityManager.persist(categorie);

		final Critere critere = new Critere();
		critere.setClef("key");
		critere.setValeur("value");
		critere.setCategorie(categorie);
		critere.setNiveauBean(niveau);
		entityManager.persist(critere);

		critereId = critere.getId();
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<Critere> result = critereRepository.findById(critereId);
		assertTrue(result.isPresent(), "We should have found our Critere");

		final Critere critere = result.get();
		assertEquals("key", critere.getClef());
		assertEquals("value", critere.getValeur());
		assertEquals(1, critere.getCategorie().getType());
		assertEquals(1, critere.getNiveauBean().getValue());
	}

}