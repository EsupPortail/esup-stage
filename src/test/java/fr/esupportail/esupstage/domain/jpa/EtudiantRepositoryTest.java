package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
		student.setDateCreation(Calendar.getInstance().getTime());
		student.setMail("ogier.ducharme@univ.fr");
		student.setIdentEtudiant("oducha01");
		student.setNumEtudiant("65299292");
		student.setNumSS("178033684913953");
		student.setLoginCreation("root");

		this.entityManager.persist(student);
		this.entityManager.flush();
	}

	private void testStudentFields(int indice, Etudiant student) {
		switch (indice) {
		case 0:
			assertEquals("ogier.ducharme@univ.fr", student.getMail(), "Student mail match");
			assertEquals("Ogier", student.getPrenom(), "Student firstname match");
			assertEquals("Ducharme", student.getNom(), "Student lastname match");
			assertEquals("M", student.getCodeSexe(), "Student sexe match");
			assertEquals("ULR", student.getCodeUniversite(), "Student University match");
			assertTrue(student.getDateCreation().before(Calendar.getInstance().getTime()) && student.getDateCreation().after(this.startTime), "Student creation date is possible");
			assertEquals(new GregorianCalendar(1978, 03, 28).getTime(), student.getDateNais(), "Student birth date match");
			assertEquals("oducha01", student.getIdentEtudiant(), "Student login match");
			assertEquals("65299292", student.getNumEtudiant(), "Student number match");
			assertEquals("178033684913953", student.getNumSS(), "Student Social Security Number match");
			assertEquals("root", student.getLoginCreation(), "Student Login creation match");
			break;
		}
	}

	@Test
	@DisplayName("findEtudiantByIdentEtudiantAndCodeUniversite – Nominal test case")
	void findById() {
		final Optional<Etudiant> result = this.studentRepository.findEtudiantByIdentEtudiantAndCodeUniversite("jdoe@uphf.fr", "ULR");
		assertTrue(result.isPresent(), "We should have found our Student");

		final Etudiant student = result.get();
		this.testStudentFields(0, student);
	}

	@Test
	@DisplayName("findEtudiantsByNomContainsOrPrenomContains – Nominal test case")
	void findEtudiantsByNomContainsOrPrenomContains() {
		final List<Etudiant> result = this.studentRepository.findEtudiantsByNomContainsOrPrenomContains("doe", "j");
		assertTrue(result.size() == 1, "We should have found our Student");

		final Etudiant student = result.get(0);
		this.testStudentFields(0, student);
	}

	@Test
	@DisplayName("findEtudiantByIdentEtudiantAndCodeUniversite – Nominal test case")
	void findEtudiantByIdentEtudiantAndCodeUniversite() {
		final Optional<Etudiant> result = this.studentRepository.findEtudiantByIdentEtudiantAndCodeUniversite("jdoe@uphf.fr", "ULR");
		assertTrue(result.isPresent(), "We should have found our Student");

		final Etudiant student = result.get();
		assertTrue(student != null, "Student exist");
		this.testStudentFields(0, student);
	}

}
