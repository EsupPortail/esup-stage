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
import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;
import fr.esupportail.esupstage.domain.jpa.repositories.ConfidentialiteRepository;

@Rollback
@Transactional
class ConfidentialiteRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final ConfidentialiteRepository confidentialiteRepository;

	private String confidentialiteId;

	@Autowired
	ConfidentialiteRepositoryTest(final EntityManager entityManager, final ConfidentialiteRepository confidentialiteRepository) {
		super();
		this.entityManager = entityManager;
		this.confidentialiteRepository = confidentialiteRepository;
	}

	@BeforeEach
	void prepare() {
		final Confidentialite confidentialite = new Confidentialite();
		confidentialite.setCode("C");
		confidentialite.setLabel("libel");
		confidentialite.setTemEnServ("C");

		entityManager.persist(confidentialite);

		confidentialiteId = confidentialite.getCode();
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<Confidentialite> result = confidentialiteRepository.findById(confidentialiteId);
		assertTrue(result.isPresent(), "We should have found our Confidentialite");

		final Confidentialite confidentialite = result.get();
		assertEquals("C", confidentialite.getCode());
		assertEquals("libel", confidentialite.getLabel());
		assertEquals("C", confidentialite.getTemEnServ());
	}

}
