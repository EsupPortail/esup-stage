package fr.esupportail.esupstage.domain.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
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
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauFormation;
import fr.esupportail.esupstage.domain.jpa.entities.Offre;
import fr.esupportail.esupstage.domain.jpa.entities.Pays;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.entities.TypeOffre;
import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.NiveauFormationRepository;

@Rollback
@Transactional
class NiveauFormationRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final NiveauFormationRepository niveauFormationRepository;

	private Integer lastInsertedId;

	@Autowired
	NiveauFormationRepositoryTest(final EntityManager entityManager, final NiveauFormationRepository niveauFormationRepository) {
		super();
		this.entityManager = entityManager;
		this.niveauFormationRepository = niveauFormationRepository;
	}

	@BeforeEach
	void prepare() {
		final NiveauFormation niveauFormation = new NiveauFormation();
		niveauFormation.setLibelleNiveauFormation("libelleNiveauFormation");
		niveauFormation.setModifiable(true);
		niveauFormation.setTemEnServNiveauForm("A");

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

		final Structure structure = new Structure();
		structure.setDateCreation(Calendar.getInstance().getTime());
		structure.setEstValidee(1);
		structure.setLoginCreation("root");
		structure.setRaisonSociale("raisonSociale");
		structure.setVoie("voie");

		final Pays pays = new Pays();
		pays.setActual(1);
		pays.setLib("lib");
		pays.setCog(1);
		pays.setSiretObligatoire(true);
		pays.setTemEnServPays("A");
		entityManager.persist(pays);

		structure.setPay(pays);
		final Effectif effectifStructure = new Effectif();
		effectifStructure.setLibelleEffectif("effectif");
		effectifStructure.setTemEnServEffectif("A");
		entityManager.persist(effectifStructure);

		structure.setEffectif(effectifStructure);
		final TypeStructure typeStructure = new TypeStructure();
		typeStructure.setLibelleTypeStructure("type1");
		typeStructure.setTemEnServTypeStructure("A");
		entityManager.persist(typeStructure);

		structure.setTypeStructure(typeStructure);
		entityManager.persist(structure);

		offre.setStructure(structure);
		final TypeOffre typeOffre = new TypeOffre();
		typeOffre.setCodeCtrl("codeCtrl");
		typeOffre.setLibelleType("libelleType");
		typeOffre.setTemEnServTypeOffre("A");
		entityManager.persist(typeOffre);

		offre.setTypeOffre(typeOffre);
		entityManager.persist(offre);

		niveauFormation.setOffres(Arrays.asList(offre));

		this.entityManager.persist(niveauFormation);
		this.entityManager.flush();

		this.entityManager.refresh(niveauFormation);
		this.lastInsertedId = niveauFormation.getId();
	}

	private void testNiveauFormationFields(int indice, NiveauFormation niveauFormation) {
		switch (indice) {
		case 0:
			assertEquals(this.lastInsertedId, niveauFormation.getId(), "NiveauFormation libelle match");
			assertEquals("libelleNiveauFormation", niveauFormation.getLibelleNiveauFormation(), "NiveauFormation libelle match");
			assertEquals("A", niveauFormation.getTemEnServNiveauForm(), "NiveauFormation TemEnServ match");
			assertEquals(1, niveauFormation.getOffres().size(), "NiveauFormation.Offre size match");
			assertEquals("Offre1", niveauFormation.getOffres().get(0).getIntitule(), "NiveauFormation.Offre codeuniv match");
			break;
		}
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<NiveauFormation> result = this.niveauFormationRepository.findById(this.lastInsertedId);
		assertTrue(result.isPresent(), "We should have found our NiveauFormation");

		final NiveauFormation niveauFormation = result.get();
		this.testNiveauFormationFields(0, niveauFormation);
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll() {
		final List<NiveauFormation> result = this.niveauFormationRepository.findAll();
		assertTrue(result.size() == 1, "We should have found our NiveauFormation");

		final NiveauFormation niveauFormation = result.get(0);
		assertTrue(niveauFormation != null, "NiveauFormation exist");
		this.testNiveauFormationFields(0, niveauFormation);
	}

}
