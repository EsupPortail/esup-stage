package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
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
import fr.esupportail.esupstage.domain.jpa.entities.Effectif;
import fr.esupportail.esupstage.domain.jpa.entities.FAP_Qualification;
import fr.esupportail.esupstage.domain.jpa.entities.FAP_QualificationSimplifiee;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.entities.Offre;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.entities.TypeOffre;
import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.FAP_QualificationSimplifieeRepository;

@Rollback
@Transactional
class FAP_QualificationSimplifieeRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final FAP_QualificationSimplifieeRepository fapQualificationSimplifieeRepository;

	@Autowired
	FAP_QualificationSimplifieeRepositoryTest(final EntityManager entityManager, final FAP_QualificationSimplifieeRepository fapQualificationSimplifieeRepository) {
		super();
		this.entityManager = entityManager;
		this.fapQualificationSimplifieeRepository = fapQualificationSimplifieeRepository;
	}

	@BeforeEach
	void prepare() {
		final FAP_Qualification fapQualification = new FAP_Qualification();

		fapQualification.setNumFAP_Qualification(1);
		fapQualification.setLibelleQualification("fapQual1");

		final FAP_QualificationSimplifiee fapQualificationSimplifiee = new FAP_QualificationSimplifiee();
		fapQualificationSimplifiee.setIdQualificationSimplifiee(1);
		fapQualificationSimplifiee.setLibelleQualification("fapQualSimple1");

		final Offre offre = new Offre();
		offre.setAnneeUniversitaire("2020-2021");
		offre.setAnneeDebut("2020");
		offre.setAvecFichier(false);
		offre.setAvecLien(false);
		offre.setCacherEtablissement(false);
		offre.setCacherFaxContactCand(false);
		offre.setCacherFaxContactInfo(false);
		offre.setCacherMailContactCand(false);
		offre.setCacherMailContactInfo(false);
		offre.setCacherNomContactCand(false);
		offre.setCacherNomContactInfo(false);
		offre.setCacherTelContactCand(false);
		offre.setCacherTelContactInfo(false);
		offre.setDateCreation(Calendar.getInstance().getTime());
		offre.setDeplacement(true);
		offre.setDescription("desc");
		offre.setEstAccessERQTH(true);
		offre.setEstDiffusee(false);
		offre.setEstPourvue(true);
		offre.setEstPrioERQTH(false);
		offre.setEstSupprimee(false);
		offre.setEstValidee(false);
		offre.setEtatValidation(0);
		offre.setIntitule("Offre1");
		offre.setOffrePourvueEtudiantLocal(true);
		offre.setPermis(true);
		offre.setRemuneration(true);
		offre.setVoiture(true);

		final NiveauCentre niveauCentre = new NiveauCentre();
		niveauCentre.setLibelleNiveauCentre("libel");
		niveauCentre.setTemEnServNiveauCentre("A");
		entityManager.persist(niveauCentre);

		final Confidentialite confidentialite = new Confidentialite();
		confidentialite.setCodeConfidentialite("A");
		confidentialite.setLibelleConfidentialite("libel");
		confidentialite.setTemEnServConfid("A");
		entityManager.persist(confidentialite);

		final CentreGestion centreGestion = new CentreGestion();
		centreGestion.setAutorisationEtudiantCreationConvention(true);
		centreGestion.setCodeUniversite("codeuniv");
		centreGestion.setDateCreation(Calendar.getInstance().getTime());
		centreGestion.setIdModeValidationStage(1);
		centreGestion.setLoginCreation("login");
		centreGestion.setConfidentialite(confidentialite);
		centreGestion.setNiveauCentre(niveauCentre);
		entityManager.persist(centreGestion);

		offre.setCentreGestion(centreGestion);

		final Structure structure = new Structure();

		final Effectif effectifStructure = new Effectif();
		effectifStructure.setLibelleEffectif("effectif");
		entityManager.persist(effectifStructure);

		structure.setEffectif(effectifStructure);
		final TypeStructure typeStructure = new TypeStructure();
		typeStructure.setLibelleTypeStructure("type1");
		entityManager.persist(typeStructure);

		structure.setTypeStructure(typeStructure);
		entityManager.persist(structure);

		offre.setStructure(structure);
		final TypeOffre typeOffre = new TypeOffre();
		typeOffre.setCodeCtrl("codeCtrl");
		typeOffre.setLibelleType("libelleType");
		typeOffre.setTemEnServTypeOffre("temEnServTypeOffre");
		entityManager.persist(typeOffre);

		offre.setTypeOffre(typeOffre);

		fapQualificationSimplifiee.addOffre(offre);
		entityManager.persist(offre);

		fapQualification.setFapQualificationSimplifiee(fapQualificationSimplifiee);
		entityManager.persist(fapQualificationSimplifiee);

		this.entityManager.persist(fapQualification);
		this.entityManager.flush();
	}

	private void testfapQualSimplFields(int indice, FAP_QualificationSimplifiee fapQualification) {
		switch (indice) {
		case 0:
			assertEquals("fapQualSimple1", fapQualification.getLibelleQualification(), "FAP_Qualification.QualificationSimplifiee libelle match");
			assertEquals("Offre1", fapQualification.getOffres().get(0).getIntitule(), "FAP_Qualification.QualificationSimplifiee.Offre libelle match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<FAP_QualificationSimplifiee> result = this.fapQualificationSimplifieeRepository.findById(1);
		assertTrue(result.isPresent(), "We should have found our FAP_QualificationSimplifiee");

		final FAP_QualificationSimplifiee fapQualification = result.get();
		this.testfapQualSimplFields(0, fapQualification);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<FAP_QualificationSimplifiee> result = this.fapQualificationSimplifieeRepository.findAll();
		assertTrue(result.size() == 1, "We should have found our FAP_QualificationSimplifiee");

		final FAP_QualificationSimplifiee fapQualification = result.get(0);
		assertTrue(fapQualification != null, "FAP_QualificationSimplifiee exist");
		this.testfapQualSimplFields(0, fapQualification);
	}

}
