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
import fr.esupportail.esupstage.domain.jpa.entities.Pays;
import fr.esupportail.esupstage.domain.jpa.repositories.PaysRepository;

@Rollback
@Transactional
public class PaysRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final PaysRepository repository;

	private Integer id;

	@Autowired
	PaysRepositoryTest(final EntityManager entityManager, final PaysRepository paysRepository) {
		super();
		this.entityManager = entityManager;
		this.repository = paysRepository;
	}

	@BeforeEach
	void prepare() {
		final Pays pays = new Pays();
		pays.setActual(1);
		pays.setCog(1);
		pays.setLib("Label");
		pays.setSiretObligatoire(true);
		pays.setTemEnServ("A");
		pays.setIso2("FR");

		entityManager.persist(pays);
		entityManager.flush();

		entityManager.refresh(pays);
		id = pays.getId();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<Pays> result = repository.findById(id);
		assertTrue(result.isPresent(), "We should have found our entity");

		final Pays tmp = result.get();
		assertEquals("Label", tmp.getLib());
		assertEquals(1, tmp.getActual());
		assertEquals(1, tmp.getCog());
		assertEquals("A", tmp.getTemEnServ());
		assertEquals("FR", tmp.getIso2());
		assertTrue(tmp.isSiretObligatoire());
	}

	@Test
	@DisplayName("findByIso2ContainingIgnoreCase – Nominal test case")
	void findByIso2ContainingIgnoreCase() {
		final List<Pays> result = repository.findByIso2ContainingIgnoreCase("fr");
		assertEquals(1, result.size());

		final Pays tmp = result.get(0);
		assertEquals("Label", tmp.getLib());
		assertEquals(1, tmp.getActual());
		assertEquals(1, tmp.getCog());
		assertEquals("A", tmp.getTemEnServ());
		assertEquals("FR", tmp.getIso2());
		assertTrue(tmp.isSiretObligatoire());
	}

}
