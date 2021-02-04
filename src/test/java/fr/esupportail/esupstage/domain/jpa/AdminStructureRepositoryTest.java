package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import fr.esupportail.esupstage.domain.jpa.entities.*;
import fr.esupportail.esupstage.domain.jpa.repositories.AccordPartenariatRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.AdminStructureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.data.domain.Example;
import org.springframework.test.annotation.Rollback;

import fr.esupportail.esupstage.AbstractTest;
import fr.esupportail.esupstage.domain.jpa.entities.AdminStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.AdminStructureRepository;
import static org.junit.jupiter.api.Assertions.*;


@Rollback
@Transactional
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
		adminStructure.setDateCreation(new Date(0));
		adminStructure.setLoginCreation("login");

		entityManager.persist(adminStructure);
		entityManager.flush();
		this.adminStructureId = adminStructure.getIdAdminStructure();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<AdminStructure> result = adminStructureRepository.findById(adminStructureId);
		assertTrue(result.isPresent(), "We should have found our teacher");

		final AdminStructure adminStructure = result.get();
		assertEquals(new Date(0),adminStructure.getDateCreation());
		assertEquals("login",adminStructure.getLoginCreation());
	}

}
