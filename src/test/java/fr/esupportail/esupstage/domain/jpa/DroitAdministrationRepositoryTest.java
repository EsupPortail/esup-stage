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
import fr.esupportail.esupstage.domain.jpa.entities.DroitAdministration;
import fr.esupportail.esupstage.domain.jpa.repositories.DroitAdministrationRepository;

@Rollback
@Transactional
class DroitAdministrationRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final DroitAdministrationRepository droitAdministrationRepository;

	private int droitAdministrationId;

	@Autowired
	DroitAdministrationRepositoryTest(final EntityManager entityManager, final DroitAdministrationRepository droitAdministrationRepository) {
		super();
		this.entityManager = entityManager;
		this.droitAdministrationRepository = droitAdministrationRepository;
	}

	@BeforeEach
	void prepare() {

		final DroitAdministration droitAdministration = new DroitAdministration();
		droitAdministration.setLabel("libel");
		droitAdministration.setTemEnServ("F");
		entityManager.persist(droitAdministration);

		droitAdministrationId = droitAdministration.getId();
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<DroitAdministration> result = droitAdministrationRepository.findById(droitAdministrationId);
		assertTrue(result.isPresent(), "We should have found our DroitAdministration");

		final DroitAdministration droitAdministration = result.get();
		assertEquals("libel", droitAdministration.getLabel());
		assertEquals("F", droitAdministration.getTemEnServ());
	}

}