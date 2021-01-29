package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.Rollback;

import fr.esupportail.esupstage.AbstractTest;
import fr.esupportail.esupstage.domain.jpa.entities.Etudiant;
import fr.esupportail.esupstage.domain.jpa.repositories.EtudiantRepository;

@Rollback
@Transactional
class EtudiantRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final EtudiantRepository studentRepository;

	private Date startTime;

	@Autowired
	EtudiantRepositoryTest(final EntityManager entityManager, final EtudiantRepository studentRepository) {
		super();
		this.entityManager = entityManager;
		this.studentRepository = studentRepository;
	}

	@BeforeEach
	void prepare() {
		this.startTime = Calendar.getInstance().getTime();

		final Etudiant student = new Etudiant();
		student.setCodeSexe("M");
		student.setCodeUniversite("FU");
		student.setPrenom("Ogier");
		student.setNom("Ducharme");
		student.setDateNais(new GregorianCalendar(1978, 03, 28).getTime());
		student.setMail("ogier.ducharme@univ.fr");
		student.setIdentEtudiant("oducha01");
		student.setNumEtudiant("65299292");
		student.setNumSS("178033684913953");

		this.entityManager.persist(student);
		this.entityManager.flush();
	}

	@Test
	@DisplayName("findEtudiantByIdentEtudiantAndCodeUniversite – Nominal test case")
	void findById() {
		final Optional<Etudiant> result = this.studentRepository.findEtudiantByIdentEtudiantAndCodeUniversite("jdoe@uphf.fr", "ULR");
		assertTrue(result.isPresent(), "We should have found our teacher");

		final Etudiant student = result.get();
		assertEquals("ogier.ducharme@univ.fr", student.getMail());
		assertEquals("Ogier", student.getPrenom());
		assertEquals("Ducharme", student.getNom());
		assertEquals("M", student.getCodeSexe());
		assertEquals("M", student.getCodeUniversite());
		assertEquals("FU", student.getCodeSexe());
		assertTrue(student.getDateCreation().before(Calendar.getInstance().getTime()) && student.getDateCreation().after(this.startTime));
		assertEquals(new GregorianCalendar(1978, 03, 28).getTime(), student.getDateNais());
		assertEquals("ogier.ducharme@univ.fr", student.getMail());
		assertEquals("oducha01", student.getIdentEtudiant());
		assertEquals("65299292", student.getNumEtudiant());
		assertEquals("178033684913953", student.getNumSS());

		assertEquals(LocalDate.of(1980, 01, 01), student.getDateNais());
	}

}
