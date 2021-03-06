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
import fr.esupportail.esupstage.domain.jpa.entities.FAP_Qualification;
import fr.esupportail.esupstage.domain.jpa.entities.FAP_QualificationSimplifiee;
import fr.esupportail.esupstage.domain.jpa.entities.FapN1;
import fr.esupportail.esupstage.domain.jpa.entities.FapN2;
import fr.esupportail.esupstage.domain.jpa.entities.FapN3;
import fr.esupportail.esupstage.domain.jpa.repositories.FapN3Repository;

@Rollback
@Transactional
class FapN3RepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final FapN3Repository FapN3Repository;

	@Autowired
	FapN3RepositoryTest(final EntityManager entityManager, final FapN3Repository FapN3Repository) {
		super();
		this.entityManager = entityManager;
		this.FapN3Repository = FapN3Repository;
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

		final FapN3 fapN3 = new FapN3();
		fapN3.setCode("code3");
		fapN3.setLabel("libelleFAP_N3");
		fapN3.setFapN2(fapN2);

		final FAP_Qualification fapQualification = new FAP_Qualification();
		fapQualification.setLabel("fapQual1");

		final FAP_QualificationSimplifiee fapQualificationSimplifiee = new FAP_QualificationSimplifiee();
		fapQualificationSimplifiee.setLabel("fapQualSimple1");

		entityManager.persist(fapQualificationSimplifiee);

		fapQualification.setFapQualificationSimplifiee(fapQualificationSimplifiee);

		this.entityManager.persist(fapQualification);

		fapN3.setFapQualification(fapQualification);

		this.entityManager.persist(fapN1);
		this.entityManager.persist(fapN2);
		this.entityManager.persist(fapN3);

		this.entityManager.flush();
	}

	private void testFapFields(int indice, FapN3 fap) {
		switch (indice) {
		case 0:
			assertEquals("code3", fap.getCode(), "FapN3 code match");
			assertEquals("libelleFAP_N3", fap.getLabel(), "FapN3 libelle match");
			break;
		}
	}

	@Test
	@DisplayName("findById ??? Nominal test case")
	void findById() {
		final Optional<FapN3> result = this.FapN3Repository.findById("code3");
		assertTrue(result.isPresent(), "We should have found our FapN3");

		final FapN3 fap = result.get();
		this.testFapFields(0, fap);
	}

	@Test
	@DisplayName("findAll ??? Nominal test case")
	void findAll() {
		final List<FapN3> result = this.FapN3Repository.findAll();
		assertEquals(1, result.size(), "We should have found our FapN3");

		final FapN3 fapQualification = result.get(0);
		assertNotNull(fapQualification, "FapN3 exist");
		this.testFapFields(0, fapQualification);
	}

}
