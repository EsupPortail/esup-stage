package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import fr.esupportail.esupstage.domain.jpa.repositories.AdminStructureRepository;


@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
class AdminStructureRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;
	private final AdminStructureRepository adminStructureRepository;
	private int adminStructureId;

	@Autowired
	AdminStructureRepositoryTest(final EntityManager entityManager, final AdminStructureRepository adminStructureRepository) {
		super();
		this.entityManager = entityManager;
		this.adminStructureRepository = adminStructureRepository;
	}

	@BeforeEach
	void prepare() {

		final AdminStructure adminStructure = new AdminStructure();

		entityManager.persist(adminStructure);
		entityManager.flush();
		this.adminStructureId = adminStructure.getId();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<AdminStructure> result = adminStructureRepository.findById(adminStructureId);
		assertTrue(result.isPresent(), "We should have found our teacher");

		final AdminStructure adminStructure = result.get();
		assertNotNull(adminStructure.getCreatedDate());
		assertEquals("jdoe",adminStructure.getCreatedBy());
	}

}
