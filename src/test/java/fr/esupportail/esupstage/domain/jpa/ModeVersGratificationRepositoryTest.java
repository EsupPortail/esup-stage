package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import fr.esupportail.esupstage.domain.jpa.entities.ModeVersGratification;
import fr.esupportail.esupstage.domain.jpa.repositories.ModeVersGratificationRepository;

@Rollback
@Transactional
class ModeVersGratificationRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final ModeVersGratificationRepository modeVersGratificationRepository;

	private Integer lastInsertedId;

	@Autowired
	ModeVersGratificationRepositoryTest(final EntityManager entityManager, final ModeVersGratificationRepository modeVersGratificationRepository) {
		super();
		this.entityManager = entityManager;
		this.modeVersGratificationRepository = modeVersGratificationRepository;
	}

	@BeforeEach
	void prepare() {
		ModeVersGratification modeVersGratification = new ModeVersGratification();
		modeVersGratification.setLabel("libelleModeVersGratification");
		modeVersGratification.setTemEnServ("A");
		entityManager.persist(modeVersGratification);

		this.entityManager.flush();
		this.entityManager.refresh(modeVersGratification);
		this.lastInsertedId = modeVersGratification.getId();
	}

	private void testModeVersGratificationFields(int indice, ModeVersGratification modeVersGratification) {
		switch (indice) {
		case 0:
			assertEquals(this.lastInsertedId, modeVersGratification.getId(), "ModeVersGratification id match");
			assertEquals("libelleModeVersGratification", modeVersGratification.getLabel(), "ModeVersGratification libelle match");
			assertEquals("A", modeVersGratification.getTemEnServ(), "ModeVersGratification temEnServ match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<ModeVersGratification> result = this.modeVersGratificationRepository.findById(this.lastInsertedId);
		assertTrue(result.isPresent(), "We should have found our ModeVersGratification");

		final ModeVersGratification modeCandidature = result.get();
		this.testModeVersGratificationFields(0, modeCandidature);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<ModeVersGratification> result = this.modeVersGratificationRepository.findAll();
		assertEquals(1, result.size(), "We should have found our ModeVersGratification");

		final ModeVersGratification modeCandidature = result.get(0);
		assertNotNull(modeCandidature, "ModeVersGratification exist");
		this.testModeVersGratificationFields(0, modeCandidature);
	}

}
