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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;

import fr.esupportail.esupstage.AbstractTest;
import fr.esupportail.esupstage.domain.jpa.entities.AdminStructure;
import fr.esupportail.esupstage.domain.jpa.entities.Civilite;
import fr.esupportail.esupstage.domain.jpa.repositories.CiviliteRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
class CiviliteRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final CiviliteRepository civiliteRepository;

	private int civiliteId;

	@Autowired
	CiviliteRepositoryTest(final EntityManager entityManager, final CiviliteRepository civiliteRepository) {
		super();
		this.entityManager = entityManager;
		this.civiliteRepository = civiliteRepository;
	}

	@BeforeEach
	void prepare() {
		final AdminStructure adminStructure = new AdminStructure();
		entityManager.persist(adminStructure);

		final Civilite civilite = new Civilite();
		civilite.setLibelleCivilite("libel");
		civilite.addAdminStructure(adminStructure);
		entityManager.persist(civilite);

		civiliteId = civilite.getIdCivilite();
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<Civilite> result = civiliteRepository.findById(civiliteId);
		assertTrue(result.isPresent(), "We should have found our Civilite");

		final Civilite civilite = result.get();
		assertEquals("jdoe", civilite.getAdminStructures().get(0).getCreatedBy());
		assertEquals("libel", civilite.getLibelleCivilite());
	}

}
