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
import org.springframework.test.annotation.Rollback;

import fr.esupportail.esupstage.AbstractTest;
import fr.esupportail.esupstage.domain.jpa.entities.TypeOffre;
import fr.esupportail.esupstage.domain.jpa.repositories.TypeOffreRepository;

@Rollback
@Transactional
public class TypeOffreRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final TypeOffreRepository repository;

	private Integer id;

	@Autowired
	TypeOffreRepositoryTest(final EntityManager entityManager, final TypeOffreRepository repository) {
		super();
		this.entityManager = entityManager;
		this.repository = repository;
	}

	@BeforeEach
	void prepare() {
		final TypeOffre typeOffre = new TypeOffre();
		typeOffre.setCodeCtrl("Code");
		typeOffre.setLabel("Label");
		typeOffre.setTemEnServ("A");
		entityManager.persist(typeOffre);

		entityManager.flush();
		entityManager.refresh(typeOffre);
		id = typeOffre.getId();
	}

	@Test
	@DisplayName("findById – Nominal Test Case")
	void findById() {
		final Optional<TypeOffre> result = repository.findById(id);
		assertTrue(result.isPresent(), "We should have found our entity");

		final TypeOffre tmp = result.get();
		assertEquals("Code", tmp.getCodeCtrl());
		assertEquals("Label", tmp.getLabel());
		assertEquals("A", tmp.getTemEnServ());
	}

}
