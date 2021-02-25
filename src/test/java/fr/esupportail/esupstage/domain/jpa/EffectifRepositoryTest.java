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
import fr.esupportail.esupstage.domain.jpa.entities.Effectif;
import fr.esupportail.esupstage.domain.jpa.repositories.EffectifRepository;

@Rollback
@Transactional
class EffectifRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final EffectifRepository effectifRepository;

	private int effectId;

	@Autowired
	EffectifRepositoryTest(final EntityManager entityManager, final EffectifRepository effectifRepository) {
		super();
		this.entityManager = entityManager;
		this.effectifRepository = effectifRepository;
	}

	@BeforeEach
	void prepare() {

		final Effectif effectif = new Effectif();
		effectif.setLabel("libel");
		effectif.setTemEnServ("L");
		entityManager.persist(effectif);

		effectId = effectif.getId();
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<Effectif> result = effectifRepository.findById(effectId);
		assertTrue(result.isPresent(), "We should have found our Effectif");

		final Effectif effectif = result.get();
		assertEquals("libel", effectif.getLabel());
		assertEquals("L", effectif.getTemEnServ());
	}

}
