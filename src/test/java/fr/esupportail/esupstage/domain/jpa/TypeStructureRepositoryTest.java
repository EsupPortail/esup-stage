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
import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.TypeStructureRepository;

@Rollback
@Transactional
public class TypeStructureRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final TypeStructureRepository repository;

	private Integer id;

	@Autowired
	TypeStructureRepositoryTest(final EntityManager entityManager, final TypeStructureRepository repository) {
		super();
		this.entityManager = entityManager;
		this.repository = repository;
	}

	@BeforeEach
	void prepare() {
		final TypeStructure typeStructure = new TypeStructure();
		typeStructure.setLabel("Label");
		typeStructure.setSiretObligatoire(true);
		typeStructure.setTemEnServ("A");
		entityManager.persist(typeStructure);

		entityManager.flush();
		entityManager.refresh(typeStructure);
		id = typeStructure.getId();
	}

	@Test
	@DisplayName("findById – Nominal Test Case")
	void findById() {
		final Optional<TypeStructure> result = repository.findById(id);
		assertTrue(result.isPresent(), "We should have found our entity");

		final TypeStructure tmp = result.get();
		assertTrue(tmp.isSiretObligatoire());
		assertEquals("Label", tmp.getLabel());
		assertEquals("A", tmp.getTemEnServ());
	}

}
