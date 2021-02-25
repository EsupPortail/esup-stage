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
import fr.esupportail.esupstage.domain.jpa.entities.Effectif;
import fr.esupportail.esupstage.domain.jpa.entities.Pays;
import fr.esupportail.esupstage.domain.jpa.entities.Service;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.ServiceRepository;

@Rollback
@Transactional
@WithMockUser(username = "jdoe", password = "jdoe")
public class ServiceRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final ServiceRepository repository;

	private Integer id;

	@Autowired
	ServiceRepositoryTest(final EntityManager entityManager, final ServiceRepository repository) {
		super();
		this.entityManager = entityManager;
		this.repository = repository;
	}

	@BeforeEach
	void prepare() {
		final Pays pays = new Pays();
		pays.setActual(1);
		pays.setCog(1);
		pays.setLib("Label");
		pays.setSiretObligatoire(true);
		pays.setTemEnServ("A");
		entityManager.persist(pays);

		final Effectif effectif = new Effectif();
		effectif.setLabel("Label");
		effectif.setTemEnServ("A");
		entityManager.persist(effectif);

		final TypeStructure typeStructure = new TypeStructure();
		typeStructure.setTemEnServ("A");
		typeStructure.setLabel("Label");
		typeStructure.setSiretObligatoire(true);
		entityManager.persist(typeStructure);

		final Structure structure = new Structure();
		structure.setEstValidee(1);
		structure.setRaisonSociale("ESN");
		structure.setVoie("Street");
		structure.setPay(pays);
		structure.setEffectif(effectif);
		structure.setTypeStructure(typeStructure);
		entityManager.persist(structure);

		final Service service = new Service();
		service.setCodePostal("59000");
		service.setNom("Service");
		service.setVoie("Street");
		service.setPay(pays);
		service.setStructure(structure);
		entityManager.persist(service);
		entityManager.flush();

		entityManager.refresh(service);
		id = service.getId();
	}

	@Test
	@DisplayName("findById – Nominal Test Case")
	void findById() {
		final Optional<Service> result = repository.findById(id);
		assertTrue(result.isPresent(), "We should have found our entity");

		final Service tmp = result.get();
		assertEquals("jdoe", tmp.getCreatedBy());
		assertEquals("59000", tmp.getCodePostal());
		assertEquals("Service", tmp.getNom());
		assertEquals("Street", tmp.getVoie());
	}

}
