package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
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
import fr.esupportail.esupstage.domain.jpa.entities.Pays;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.entities.TypeOffre;
import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.FAP_QualificationRepository;

@Rollback
@Transactional
class FAP_QualificationRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final FAP_QualificationRepository fapQualificationRepository;

	private Integer lastInsertedId;

	@Autowired
	FAP_QualificationRepositoryTest(final EntityManager entityManager, final FAP_QualificationRepository fapQualificationRepository) {
		super();
		this.entityManager = entityManager;
		this.fapQualificationRepository = fapQualificationRepository;
	}

	@BeforeEach
	void prepare() {
		final FAP_Qualification fapQualification = new FAP_Qualification();

		fapQualification.setLibelleQualification("fapQual1");

		final FAP_QualificationSimplifiee fapQualificationSimplifiee = new FAP_QualificationSimplifiee();
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
		offre.setLoginCreation("root");

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

		final Pays pays = new Pays();
		pays.setActual(1);
		pays.setLib("lib");
		pays.setTemEnServPays("A");
		pays.setCog(1);
		entityManager.persist(pays);

		final Effectif effectifStructure = new Effectif();
		effectifStructure.setLibelleEffectif("effectif");
		effectifStructure.setTemEnServEffectif("A");
		entityManager.persist(effectifStructure);

		final TypeStructure typeStructure = new TypeStructure();
		typeStructure.setLibelleTypeStructure("type1");
		typeStructure.setTemEnServTypeStructure("A");
		entityManager.persist(typeStructure);

		final Structure structure = new Structure();
		structure.setEstValidee(1);
		structure.setRaisonSociale("raison");
		structure.setVoie("voie");
		structure.setEffectif(effectifStructure);
		structure.setPay(pays);
		structure.setTypeStructure(typeStructure);
		entityManager.persist(structure);
		structure.setTypeStructure(typeStructure);
		entityManager.persist(structure);

		offre.setStructure(structure);
		final TypeOffre typeOffre = new TypeOffre();
		typeOffre.setCodeCtrl("codeCtrl");
		typeOffre.setLibelleType("libelleType");
		typeOffre.setTemEnServTypeOffre("A");
		entityManager.persist(typeOffre);

		offre.setTypeOffre(typeOffre);

		fapQualificationSimplifiee.setOffres(Arrays.asList(offre));
		entityManager.persist(offre);

		fapQualification.setFapQualificationSimplifiee(fapQualificationSimplifiee);
		entityManager.persist(fapQualificationSimplifiee);

		this.entityManager.persist(fapQualification);
		this.entityManager.flush();

		this.entityManager.refresh(fapQualification);
		this.lastInsertedId = fapQualification.getNumFAP_Qualification();
	}

	private void testfapQualFields(int indice, FAP_Qualification fapQualification) {
		switch (indice) {
		case 0:
			assertEquals("fapQual1", fapQualification.getLibelleQualification(), "FAP_Qualification libelle match");
			assertEquals(this.lastInsertedId, fapQualification.getNumFAP_Qualification(), "FAP_Qualification number match");
			assertEquals("fapQualSimple1", fapQualification.getFapQualificationSimplifiee().getLibelleQualification(), "FAP_Qualification.QualificationSimplifiee libelle match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<FAP_Qualification> result = this.fapQualificationRepository.findById(this.lastInsertedId);
		assertTrue(result.isPresent(), "We should have found our FAP_Qualification");

		final FAP_Qualification fapQualification = result.get();
		this.testfapQualFields(0, fapQualification);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<FAP_Qualification> result = this.fapQualificationRepository.findAll();
		assertTrue(result.size() == 1, "We should have found our FAP_Qualification");

		final FAP_Qualification fapQualification = result.get(0);
		assertTrue(fapQualification != null, "FAP_Qualification exist");
		this.testfapQualFields(0, fapQualification);
	}

}
