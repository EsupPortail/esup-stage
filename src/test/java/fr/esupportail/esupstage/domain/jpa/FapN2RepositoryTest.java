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
import fr.esupportail.esupstage.domain.jpa.entities.FapN1;
import fr.esupportail.esupstage.domain.jpa.entities.FapN2;
import fr.esupportail.esupstage.domain.jpa.repositories.FapN2Repository;

@Rollback
@Transactional
class FapN2RepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final FapN2Repository fapN2Repository;

	@Autowired
	FapN2RepositoryTest(final EntityManager entityManager, final FapN2Repository fapN2Repository) {
		super();
		this.entityManager = entityManager;
		this.fapN2Repository = fapN2Repository;
	}

	@BeforeEach
	void prepare() {
		final FapN1 fapN1 = new FapN1();
		fapN1.setCode("1");
		fapN1.setLabel("libelleFAP_N1");

		final FapN2 fapN2 = new FapN2();
		fapN2.setCode("co2");
		fapN2.setLabel("libelleFAP_N2");
		fapN2.setFapN1(fapN1);

		this.entityManager.persist(fapN1);
		this.entityManager.persist(fapN2);

		this.entityManager.flush();
	}

	private void testFapFields(int indice, FapN2 fap) {
		switch (indice) {
		case 0:
			assertEquals("co2", fap.getCode(), "FapN2 code match");
			assertEquals("libelleFAP_N2", fap.getLabel(), "FapN2 libelle match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<FapN2> result = this.fapN2Repository.findById("co2");
		assertTrue(result.isPresent(), "We should have found our FapN2");

		final FapN2 fapQualification = result.get();
		this.testFapFields(0, fapQualification);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<FapN2> result = this.fapN2Repository.findAll();
		assertEquals(1, result.size(), "We should have found our FapN2");

		final FapN2 fapQualification = result.get(0);
		assertNotNull(fapQualification, "FapN2 exist");
		this.testFapFields(0, fapQualification);
	}

}
