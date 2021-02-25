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
import fr.esupportail.esupstage.domain.jpa.entities.TempsTravail;
import fr.esupportail.esupstage.domain.jpa.repositories.TempsTravailRepository;

@Rollback
@Transactional
public class TempsTravailRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final TempsTravailRepository repository;

	private Integer id;

	@Autowired
	TempsTravailRepositoryTest(final EntityManager entityManager, final TempsTravailRepository repository) {
		super();
		this.entityManager = entityManager;
		this.repository = repository;
	}

	@BeforeEach
	void prepare() {
		final TempsTravail tempsTravail = new TempsTravail();
		tempsTravail.setCodeCtrl("Code");
		tempsTravail.setLabel("Label");
		tempsTravail.setTemEnServ("A");
		entityManager.persist(tempsTravail);

		entityManager.flush();
		entityManager.refresh(tempsTravail);
		id = tempsTravail.getId();
	}

	@Test
	@DisplayName("findById – Nominal Test Case")
	void findById() {
		final Optional<TempsTravail> result = repository.findById(id);
		assertTrue(result.isPresent(), "We should have found our entity");

		final TempsTravail tmp = result.get();
		assertEquals("Code", tmp.getCodeCtrl());
		assertEquals("Label", tmp.getLabel());
		assertEquals("A", tmp.getTemEnServ());
	}

}
