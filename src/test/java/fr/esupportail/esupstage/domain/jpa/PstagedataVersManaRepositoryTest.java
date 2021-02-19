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
import fr.esupportail.esupstage.domain.jpa.entities.PstagedataVersMana;
import fr.esupportail.esupstage.domain.jpa.repositories.PstagedataVersManaRepository;

@Rollback
@Transactional
public class PstagedataVersManaRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final PstagedataVersManaRepository repository;

	@Autowired
	PstagedataVersManaRepositoryTest(final EntityManager entityManager, final PstagedataVersManaRepository repository) {
		super();
		this.entityManager = entityManager;
		this.repository = repository;
	}

	@BeforeEach
	void prepare() {
		final PstagedataVersMana entity = new PstagedataVersMana();
		entity.setId(1L);
		entity.setVers("1");

		entityManager.persist(entity);
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<PstagedataVersMana> result = repository.findById(1L);
		assertTrue(result.isPresent(), "We should have found our entity");

		final PstagedataVersMana tmp = result.get();
		assertEquals("1", tmp.getVers());
	}

}
