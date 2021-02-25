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
import fr.esupportail.esupstage.domain.jpa.entities.Theme;
import fr.esupportail.esupstage.domain.jpa.repositories.ThemeRepository;

@Rollback
@Transactional
public class ThemeRepositoryTest extends AbstractTest {

	private final EntityManager entityManager;

	private final ThemeRepository repository;

	private Integer id;

	@Autowired
	ThemeRepositoryTest(final EntityManager entityManager, final ThemeRepository repository) {
		super();
		this.entityManager = entityManager;
		this.repository = repository;
	}

	@BeforeEach
	void prepare() {
		final Theme theme = new Theme();
		theme.setLabel("Label");
		theme.setTemEnServ("A");
		entityManager.persist(theme);

		entityManager.flush();
		entityManager.refresh(theme);
		id = theme.getId();
	}

	@Test
	@DisplayName("findById – Nominal Test Case")
	void findById() {
		final Optional<Theme> result = repository.findById(id);
		assertTrue(result.isPresent(), "We should have found our entity");

		final Theme tmp = result.get();
		assertEquals("Label", tmp.getLabel());
		assertEquals("A", tmp.getTemEnServ());
	}

}
