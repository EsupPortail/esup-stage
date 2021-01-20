package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
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
import fr.esupportail.esupstage.domain.jpa.entities.CentreGestion;
import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;
import fr.esupportail.esupstage.domain.jpa.entities.Fichier;
import fr.esupportail.esupstage.domain.jpa.entities.NafN1;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.repositories.FichierRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.NafN1Repository;

@Rollback
@Transactional
class NafN1RepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final NafN1Repository nafN1Repository;

	@Autowired
	NafN1RepositoryTest(final EntityManager entityManager, final NafN1Repository nafN1Repository) {
		super();
		this.entityManager = entityManager;
		this.nafN1Repository = nafN1Repository;
	}

	@BeforeEach
	void prepare() {
		final NafN1 nafN1 = new NafN1();
		nafN1.setCodeNAF_N1("A");
		nafN1.setLibelleNAF_N1("libelleNAF_N1");
		nafN1.setTemEnServNAF_N1("B");

		this.entityManager.persist(nafN1);
		this.entityManager.flush();
	}

	private void testNafN1Fields(int indice, NafN1 nafN1) {
		switch (indice) {
		case 0:
			assertEquals("A", nafN1.getCodeNAF_N1(), "NafN1 code match");
			assertEquals("libelleNAF_N1", nafN1.getLibelleNAF_N1(), "NafN1 libelle match");
			assertEquals("B", nafN1.getTemEnServNAF_N1(), "NafN1 TemEnServ match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<NafN1> result = this.nafN1Repository.findById("A");
		assertTrue(result.isPresent(), "We should have found our NafN1");

		final NafN1 fichier = result.get();
		this.testNafN1Fields(0, fichier);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<NafN1> result = this.nafN1Repository.findAll();
		assertTrue(result.size() == 1, "We should have found our NafN1");

		final NafN1 fichier = result.get(0);
		assertTrue(fichier != null, "NafN1 exist");
		this.testNafN1Fields(0, fichier);
	}

}
