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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;

import fr.esupportail.esupstage.AbstractTest;
import fr.esupportail.esupstage.domain.jpa.entities.AccordPartenariat;
import fr.esupportail.esupstage.domain.jpa.entities.CentreGestion;
import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;
import fr.esupportail.esupstage.domain.jpa.entities.Contact;
import fr.esupportail.esupstage.domain.jpa.entities.Effectif;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.entities.Pays;
import fr.esupportail.esupstage.domain.jpa.entities.Service;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.AccordPartenariatRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
class AccordPartenariatRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final AccordPartenariatRepository accordPartenariatRepository;

	private int accordPartenariatId;

	@Autowired
	AccordPartenariatRepositoryTest(final EntityManager entityManager, final AccordPartenariatRepository accordPartenariatRepository) {
		super();
		this.entityManager = entityManager;
		this.accordPartenariatRepository = accordPartenariatRepository;
	}

	@BeforeEach
	void prepare() {

		final AccordPartenariat accordPartenariat = new AccordPartenariat();
		final NiveauCentre niveauCentre = new NiveauCentre();
		niveauCentre.setLabel("libel");
		niveauCentre.setTemEnServ("A");

		entityManager.persist(niveauCentre);

		final Confidentialite confidentialite = new Confidentialite();
		confidentialite.setCode("A");
		confidentialite.setLabel("libel");
		confidentialite.setTemEnServ("A");
		entityManager.persist(confidentialite);

		final CentreGestion centreGestion = new CentreGestion();
		centreGestion.setAutorisationEtudiantCreationConvention(true);
		centreGestion.setCodeUniversite("codeuniv");
		centreGestion.setIdModeValidationStage(1);
		centreGestion.setConfidentialite(confidentialite);
		centreGestion.setNiveauCentre(niveauCentre);
		entityManager.persist(centreGestion);

		final Pays pays = new Pays();
		pays.setActual(1);
		pays.setLib("lib");
		pays.setTemEnServ("A");
		pays.setCog(1);
		entityManager.persist(pays);

		final Effectif effectif = new Effectif();
		effectif.setLabel("libel");
		effectif.setTemEnServ("A");
		entityManager.persist(effectif);

		final TypeStructure typeStructure = new TypeStructure();
		typeStructure.setLabel("libel");
		typeStructure.setTemEnServ("A");
		entityManager.persist(typeStructure);

		final Structure structure = new Structure();
		structure.setEstValidee(1);
		structure.setRaisonSociale("raison");
		structure.setVoie("voie");
		structure.setEffectif(effectif);
		structure.setPay(pays);
		structure.setTypeStructure(typeStructure);
		entityManager.persist(structure);

		final Service service = new Service();
		service.setCodePostal("17000");
		service.setNom("nom service");
		service.setPay(pays);
		service.setStructure(structure);
		service.setVoie("voie");
		entityManager.persist(service);

		final Contact contact = new Contact();
		contact.setFonction("fonction");
		contact.setLogin("login");
		contact.setNom("Doe");
		contact.setPrenom("John");
		contact.setCentreGestion(centreGestion);
		contact.setService(service);
		entityManager.persist(contact);

		accordPartenariat.setContact(contact);
		accordPartenariat.setStructure(structure);

		entityManager.persist(accordPartenariat);
		accordPartenariatId = accordPartenariat.getId();
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<AccordPartenariat> result = accordPartenariatRepository.findById(accordPartenariatId);
		assertTrue(result.isPresent(), "We should have found our teacher");

		final AccordPartenariat accordPartenariat = result.get();
		assertEquals("jdoe", accordPartenariat.getCreatedBy());
		assertEquals(accordPartenariatId, accordPartenariat.getId());
		assertEquals("Doe", accordPartenariat.getContact().getNom());
		assertEquals("nom service", accordPartenariat.getContact().getService().getNom());
		assertEquals("voie", accordPartenariat.getContact().getService().getStructure().getVoie());
		assertEquals("libel", accordPartenariat.getContact().getService().getStructure().getTypeStructure().getLabel());
		assertEquals("libel", accordPartenariat.getContact().getService().getStructure().getEffectif().getLabel());
		assertEquals("lib", accordPartenariat.getContact().getService().getStructure().getPay().getLib());
		assertEquals("codeuniv", accordPartenariat.getContact().getCentreGestion().getCodeUniversite());
		assertEquals("libel", accordPartenariat.getContact().getCentreGestion().getConfidentialite().getLabel());
	}

}
