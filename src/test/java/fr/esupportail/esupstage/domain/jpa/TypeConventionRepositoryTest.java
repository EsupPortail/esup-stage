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
import fr.esupportail.esupstage.domain.jpa.entities.TypeConvention;
import fr.esupportail.esupstage.domain.jpa.repositories.TypeConventionRepository;

@Rollback
@Transactional
public class TypeConventionRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final TypeConventionRepository repository;

	private Integer id;

	@Autowired
	TypeConventionRepositoryTest(final EntityManager entityManager, final TypeConventionRepository repository) {
		super();
		this.entityManager = entityManager;
		this.repository = repository;
	}

	@BeforeEach
	void prepare() {
		final TypeConvention typeConvention = new TypeConvention();
		typeConvention.setCodeCtrl("Code");
		typeConvention.setLabel("Label");
		typeConvention.setTemEnServ("A");
		entityManager.persist(typeConvention);

		entityManager.flush();
		entityManager.refresh(typeConvention);
		id = typeConvention.getId();
	}

	@Test
	@DisplayName("findById – Nominal Test Case")
	void findById() {
		final Optional<TypeConvention> result = repository.findById(id);
		assertTrue(result.isPresent(), "We should have found our entity");

		final TypeConvention tmp = result.get();
		assertEquals("Code", tmp.getCodeCtrl());
		assertEquals("A", tmp.getTemEnServ());
		assertEquals("Label", tmp.getLabel());
	}

}
