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
import fr.esupportail.esupstage.domain.jpa.entities.ModeCandidature;
import fr.esupportail.esupstage.domain.jpa.repositories.ModeCandidatureRepository;

@Rollback
@Transactional
class ModeCandidatureRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final ModeCandidatureRepository modeCandidatureRepository;

	private int lastInsertedId;

	@Autowired
	ModeCandidatureRepositoryTest(final EntityManager entityManager, final ModeCandidatureRepository modeCandidatureRepository) {
		super();
		this.entityManager = entityManager;
		this.modeCandidatureRepository = modeCandidatureRepository;
	}

	@BeforeEach
	void prepare() {
		ModeCandidature modeCandidature = new ModeCandidature();
		modeCandidature.setCodeCtrl("codeCtrl");
		modeCandidature.setLibelleModeCandidature("libelleModeCandidature");
		modeCandidature.setTemEnServModeCandidature("A");
		entityManager.persist(modeCandidature);

		this.entityManager.flush();

		this.entityManager.refresh(modeCandidature);
		this.lastInsertedId = modeCandidature.getIdModeCandidature();
	}

	private void testModeCandidatureFields(int indice, ModeCandidature modeCandidature) {
		switch (indice) {
		case 0:
			assertEquals(this.lastInsertedId, modeCandidature.getIdModeCandidature(), "ModeCandidature id match");
			assertEquals("codeCtrl", modeCandidature.getCodeCtrl(), "ModeCandidature code match");
			assertEquals("libelleModeCandidature", modeCandidature.getLibelleModeCandidature(), "ModeCandidature libelle match");
			assertEquals("A", modeCandidature.getTemEnServModeCandidature(), "ModeCandidature temEnServ match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<ModeCandidature> result = this.modeCandidatureRepository.findById(this.lastInsertedId);
		assertTrue(result.isPresent(), "We should have found our ModeCandidature");

		final ModeCandidature modeCandidature = result.get();
		this.testModeCandidatureFields(0, modeCandidature);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<ModeCandidature> result = this.modeCandidatureRepository.findAll();
		assertTrue(result.size() == 1, "We should have found our Fichier");

		final ModeCandidature modeCandidature = result.get(0);
		assertTrue(modeCandidature != null, "ModeCandidature exist");
		this.testModeCandidatureFields(0, modeCandidature);
	}

}
