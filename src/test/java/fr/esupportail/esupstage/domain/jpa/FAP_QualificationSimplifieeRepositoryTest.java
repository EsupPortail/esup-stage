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
import fr.esupportail.esupstage.domain.jpa.repositories.FAP_QualificationSimplifieeRepository;

@Rollback
@Transactional
class FAP_QualificationSimplifieeRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final FAP_QualificationSimplifieeRepository fapQualificationSimplifieeRepository;

	private Integer lastInsertedId;

	@Autowired
	FAP_QualificationSimplifieeRepositoryTest(final EntityManager entityManager, final FAP_QualificationSimplifieeRepository fapQualificationSimplifieeRepository) {
		super();
		this.entityManager = entityManager;
		this.fapQualificationSimplifieeRepository = fapQualificationSimplifieeRepository;
	}

	@BeforeEach
	void prepare() {
		final FAP_Qualification fapQualification = new FAP_Qualification();

		fapQualification.setLibelleQualification("fapQual1");

		final FAP_QualificationSimplifiee fapQualificationSimplifiee = new FAP_QualificationSimplifiee();
		fapQualificationSimplifiee.setLibelleQualification("fapQualSimple1");
		entityManager.persist(fapQualificationSimplifiee);

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
		structure.setDateCreation(new Date());
		structure.setEstValidee(1);
		structure.setLoginCreation("login");
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
		entityManager.persist(fapQualificationSimplifiee);
		entityManager.persist(offre);

		fapQualification.setFapQualificationSimplifiee(fapQualificationSimplifiee);
		entityManager.persist(fapQualificationSimplifiee);

		this.entityManager.persist(fapQualification);
		this.entityManager.flush();

		this.entityManager.refresh(fapQualificationSimplifiee);
		this.lastInsertedId = fapQualificationSimplifiee.getId();
	}

	private void testfapQualSimplFields(int indice, FAP_QualificationSimplifiee fapQualificationSimplifiee) {
		switch (indice) {
		case 0:
			assertEquals(this.lastInsertedId, fapQualificationSimplifiee.getId(), "QualificationSimplifiee id match");
			assertEquals("fapQualSimple1", fapQualificationSimplifiee.getLibelleQualification(), "QualificationSimplifiee libelle match");
			assertEquals("Offre1", fapQualificationSimplifiee.getOffres().get(0).getIntitule(), "QualificationSimplifiee.Offre libelle match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<FAP_QualificationSimplifiee> result = this.fapQualificationSimplifieeRepository.findById(this.lastInsertedId);
		assertTrue(result.isPresent(), "We should have found our FAP_QualificationSimplifiee");

		final FAP_QualificationSimplifiee fapQualificationSimplifiee = result.get();
		this.testfapQualSimplFields(0, fapQualificationSimplifiee);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<FAP_QualificationSimplifiee> result = this.fapQualificationSimplifieeRepository.findAll();
		assertTrue(result.size() == 1, "We should have found our FAP_QualificationSimplifiee");

		final FAP_QualificationSimplifiee fapQualificationSimplifiee = result.get(0);
		assertTrue(fapQualificationSimplifiee != null, "FAP_QualificationSimplifiee exist");
		this.testfapQualSimplFields(0, fapQualificationSimplifiee);
	}

}
