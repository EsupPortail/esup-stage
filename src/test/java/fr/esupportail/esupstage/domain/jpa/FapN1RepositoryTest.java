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
import fr.esupportail.esupstage.domain.jpa.entities.FapN1;
import fr.esupportail.esupstage.domain.jpa.repositories.FapN1Repository;

@Rollback
@Transactional
class FapN1RepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final FapN1Repository fapN1Repository;

	@Autowired
	FapN1RepositoryTest(final EntityManager entityManager, final FapN1Repository fapN1Repository) {
		super();
		this.entityManager = entityManager;
		this.fapN1Repository = fapN1Repository;
	}

	@BeforeEach
	void prepare() {
		final FapN1 fapN1 = new FapN1();
		fapN1.setCodeFAP_N1("1");
		fapN1.setLibelle("libelleFAP_N1");
		this.entityManager.persist(fapN1);
		this.entityManager.flush();
	}

	private void testFapFields(int indice, FapN1 fap) {
		switch (indice) {
		case 0:
			assertEquals("1", fap.getCodeFAP_N1(), "FapN1 code match");
			assertEquals("libelleFAP_N1", fap.getLibelle(), "FapN1 libelle match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<FapN1> result = this.fapN1Repository.findById("code1");
		assertTrue(result.isPresent(), "We should have found our FapN1");

		final FapN1 fapQualification = result.get();
		this.testFapFields(0, fapQualification);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<FapN1> result = this.fapN1Repository.findAll();
		assertTrue(result.size() == 1, "We should have found our FapN1");

		final FapN1 fapQualification = result.get(0);
		assertTrue(fapQualification != null, "FapN1 exist");
		this.testFapFields(0, fapQualification);
	}

}
