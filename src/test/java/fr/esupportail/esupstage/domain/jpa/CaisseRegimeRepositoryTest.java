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
import fr.esupportail.esupstage.domain.jpa.entities.CaisseRegime;
import fr.esupportail.esupstage.domain.jpa.repositories.CaisseRegimeRepository;

@Rollback
@Transactional
class CaisseRegimeRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final CaisseRegimeRepository caisseRegimeRepository;

	private String caisseRegimeId;

	@Autowired
	CaisseRegimeRepositoryTest(final EntityManager entityManager, final CaisseRegimeRepository caisseRegimeRepository) {
		super();
		this.entityManager = entityManager;
		this.caisseRegimeRepository = caisseRegimeRepository;
	}

	@BeforeEach
	void prepare() {

		final CaisseRegime caisseRegime = new CaisseRegime();
		caisseRegime.setCodeCaisse("code");
		caisseRegime.setInfoCaisse("info");
		caisseRegime.setLabel("libel");
		caisseRegime.setTemEnServ("F");

		entityManager.persist(caisseRegime);
		caisseRegimeId = caisseRegime.getCodeCaisse();
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<CaisseRegime> result = caisseRegimeRepository.findById(caisseRegimeId);
		assertTrue(result.isPresent(), "We should have found our Assurance");

		final CaisseRegime caisseRegime = result.get();
		assertEquals("code", caisseRegime.getCodeCaisse());
		assertEquals("info", caisseRegime.getInfoCaisse());
		assertEquals("libel", caisseRegime.getLabel());
		assertEquals("F", caisseRegime.getTemEnServ());

	}

}
