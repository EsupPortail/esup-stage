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
import fr.esupportail.esupstage.domain.jpa.entities.NafN1;
import fr.esupportail.esupstage.domain.jpa.entities.NafN5;
import fr.esupportail.esupstage.domain.jpa.repositories.NafN5Repository;

@Rollback
@Transactional
class NafN5RepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final NafN5Repository nafN5Repository;

	@Autowired
	NafN5RepositoryTest(final EntityManager entityManager, final NafN5Repository nafN5Repository) {
		super();
		this.entityManager = entityManager;
		this.nafN5Repository = nafN5Repository;
	}

	@BeforeEach
	void prepare() {
		final NafN5 nafN5 = new NafN5();
		nafN5.setCodeNAF_N5("A");
		nafN5.setLibelleNAF_N5("libelleNAF_N5");
		nafN5.setTemEnServNAF_N5("B");

		final NafN1 nafN1 = new NafN1();
		nafN1.setCodeNAF_N1("C");
		nafN1.setLibelleNAF_N1("libelleNAF_N1");
		nafN1.setTemEnServNAF_N1("D");

		this.entityManager.persist(nafN1);

		nafN5.setNafN1(nafN1);
		this.entityManager.persist(nafN5);
		this.entityManager.flush();
	}

	private void testNafN5Fields(int indice, NafN5 nafN5) {
		switch (indice) {
		case 0:
			assertEquals("A", nafN5.getCodeNAF_N5(), "NafN5 code match");
			assertEquals("libelleNAF_N5", nafN5.getLibelleNAF_N5(), "NafN5 libelle match");
			assertEquals("B", nafN5.getTemEnServNAF_N5(), "NafN5 TemEnServ match");
			assertEquals("C", nafN5.getNafN1().getCodeNAF_N1(), "NafN5.NafN1 code match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<NafN5> result = this.nafN5Repository.findById("A");
		assertTrue(result.isPresent(), "We should have found our NafN5");

		final NafN5 nafN5 = result.get();
		this.testNafN5Fields(0, nafN5);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<NafN5> result = this.nafN5Repository.findAll();
		assertTrue(result.size() == 1, "We should have found our NafN5");

		final NafN5 nafN5 = result.get(0);
		assertTrue(nafN5 != null, "NafN5 exist");
		this.testNafN5Fields(0, nafN5);
	}

}
