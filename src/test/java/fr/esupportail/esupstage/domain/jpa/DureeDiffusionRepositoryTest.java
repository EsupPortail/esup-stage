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
import fr.esupportail.esupstage.domain.jpa.entities.DureeDiffusion;
import fr.esupportail.esupstage.domain.jpa.repositories.DureeDiffusionRepository;

@Rollback
@Transactional
class DureeDiffusionRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final DureeDiffusionRepository dureeDiffusionRepository;

	private int dureeDiffusionId;

	@Autowired
	DureeDiffusionRepositoryTest(final EntityManager entityManager, final DureeDiffusionRepository dureeDiffusionRepository) {
		super();
		this.entityManager = entityManager;
		this.dureeDiffusionRepository = dureeDiffusionRepository;
	}

	@BeforeEach
	void prepare() {

		final DureeDiffusion dureeDiffusion = new DureeDiffusion();
		dureeDiffusion.setLabel("libel");
		entityManager.persist(dureeDiffusion);
		entityManager.flush();

		entityManager.refresh(dureeDiffusion);
		dureeDiffusionId = dureeDiffusion.getId();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<DureeDiffusion> result = dureeDiffusionRepository.findById(dureeDiffusionId);
		assertTrue(result.isPresent(), "We should have found our DureeDiffusion");

		final DureeDiffusion dureeDiffusion = result.get();
		assertEquals("libel", dureeDiffusion.getLabel());
	}

}