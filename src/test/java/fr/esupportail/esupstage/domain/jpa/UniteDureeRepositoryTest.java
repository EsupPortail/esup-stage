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
import fr.esupportail.esupstage.domain.jpa.entities.UniteDuree;
import fr.esupportail.esupstage.domain.jpa.repositories.UniteDureeRepository;

@Rollback
@Transactional
public class UniteDureeRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final UniteDureeRepository repository;

	private Integer id;

	@Autowired
	UniteDureeRepositoryTest(final EntityManager entityManager, final UniteDureeRepository repository) {
		super();
		this.entityManager = entityManager;
		this.repository = repository;
	}

	@BeforeEach
	void prepare() {
		final UniteDuree uniteDuree = new UniteDuree();
		uniteDuree.setLabel("Label");
		uniteDuree.setTemEnServ("A");
		entityManager.persist(uniteDuree);

		entityManager.flush();
		entityManager.refresh(uniteDuree);
		id = uniteDuree.getId();
	}

	@Test
	@DisplayName("findById – Nominal Test Case")
	void findById() {
		final Optional<UniteDuree> result = repository.findById(id);
		assertTrue(result.isPresent(), "We should have found our entity");

		final UniteDuree tmp = result.get();
		assertEquals("Label", tmp.getLabel());
		assertEquals("A", tmp.getTemEnServ());
	}

}
