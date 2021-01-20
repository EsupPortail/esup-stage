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
import fr.esupportail.esupstage.domain.jpa.entities.ModeVersGratification;
import fr.esupportail.esupstage.domain.jpa.repositories.ModeVersGratificationRepository;

@Rollback
@Transactional
class ModeVersGratificationRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final ModeVersGratificationRepository modeVersGratificationRepository;

	@Autowired
	ModeVersGratificationRepositoryTest(final EntityManager entityManager, final ModeVersGratificationRepository modeVersGratificationRepository) {
		super();
		this.entityManager = entityManager;
		this.modeVersGratificationRepository = modeVersGratificationRepository;
	}

	@BeforeEach
	void prepare() {
		ModeVersGratification modeVersGratification = new ModeVersGratification();
		modeVersGratification.setLibelleModeVersGratification("libelleModeVersGratification");
		modeVersGratification.setTemEnServModeVersGrat("A");
		entityManager.persist(modeVersGratification);

		this.entityManager.flush();
	}

	private void testModeVersGratificationFields(int indice, ModeVersGratification modeVersGratification) {
		switch (indice) {
		case 0:
			assertEquals("libelleModeVersGratification", modeVersGratification.getLibelleModeVersGratification(), "ModeVersGratification libelle match");
			assertEquals("A", modeVersGratification.getTemEnServModeVersGrat(), "ModeVersGratification temEnServ match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<ModeVersGratification> result = this.modeVersGratificationRepository.findById(1);
		assertTrue(result.isPresent(), "We should have found our ModeCandidature");

		final ModeVersGratification modeCandidature = result.get();
		this.testModeVersGratificationFields(0, modeCandidature);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<ModeVersGratification> result = this.modeVersGratificationRepository.findAll();
		assertTrue(result.size() == 1, "We should have found our Fichier");

		final ModeVersGratification modeCandidature = result.get(0);
		assertTrue(modeCandidature != null, "ModeCandidature exist");
		this.testModeVersGratificationFields(0, modeCandidature);
	}

}
