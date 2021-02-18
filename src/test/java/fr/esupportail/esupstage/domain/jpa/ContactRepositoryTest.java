package fr.esupportail.esupstage.domain.jpa;

import static org.junit.Assert.assertNotNull;
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
import fr.esupportail.esupstage.domain.jpa.entities.CentreGestion;
import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;
import fr.esupportail.esupstage.domain.jpa.entities.Contact;
import fr.esupportail.esupstage.domain.jpa.entities.Effectif;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.entities.Pays;
import fr.esupportail.esupstage.domain.jpa.entities.Service;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.ContactRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
class ContactRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final ContactRepository contactRepository;

	private int contactId;

	@Autowired
	ContactRepositoryTest(final EntityManager entityManager, final ContactRepository contactRepository) {
		super();
		this.entityManager = entityManager;
		this.contactRepository = contactRepository;
	}

	@BeforeEach
	void prepare() {

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
		centreGestion.setIdModeValidationStage(1);
		centreGestion.setConfidentialite(confidentialite);
		centreGestion.setNiveauCentre(niveauCentre);
		entityManager.persist(centreGestion);

		final Pays pays = new Pays();
		pays.setActual(1);
		pays.setLib("lib");
		pays.setTemEnServPays("A");
		pays.setCog(1);
		entityManager.persist(pays);

		final Effectif effectif = new Effectif();
		effectif.setLibelleEffectif("libel");
		effectif.setTemEnServEffectif("A");
		entityManager.persist(effectif);

		final TypeStructure typeStructure = new TypeStructure();
		typeStructure.setLibelleTypeStructure("libel");
		typeStructure.setTemEnServTypeStructure("A");
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
		service.setNom("name");
		service.setTelephone("0147847584");
		service.setPay(pays);
		service.setVoie("road");
		service.setStructure(structure);
		entityManager.persist(service);

		final Contact contact = new Contact();
		contact.setFonction("function");
		contact.setNom("name");
		contact.setPrenom("firstname");
		contact.setCentreGestion(centreGestion);
		contact.setService(service);
		entityManager.persist(contact);

		contactId = contact.getIdContact();
		entityManager.flush();
	}

	@Test
	@DisplayName("findById – Nominal test case")
	void findById() {
		final Optional<Contact> result = contactRepository.findById(contactId);
		assertTrue(result.isPresent(), "We should have found our Contact");

		final Contact contact = result.get();
		assertNotNull(contact.getCreatedDate());
		assertEquals("function", contact.getFonction());
		assertEquals("jdoe", contact.getCreatedBy());
		assertEquals("name", contact.getNom());
		assertEquals("firstname", contact.getPrenom());
		assertEquals("codeuniv", contact.getCentreGestion().getCodeUniversite());
		assertEquals("17000", contact.getService().getCodePostal());
	}

}
