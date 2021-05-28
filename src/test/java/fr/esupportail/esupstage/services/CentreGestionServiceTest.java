package fr.esupportail.esupstage.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import fr.esupportail.esupstage.AbstractTest;
import fr.esupportail.esupstage.domain.jpa.entities.CentreGestion;
import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.repositories.CentreGestionRepository;
import fr.esupportail.esupstage.services.beans.CentreGestionBean;

public class CentreGestionServiceTest extends AbstractTest {

	private CentreGestionService service;

	@Mock
	private CentreGestionRepository centreGestionRepository;

	@BeforeEach
	void prepare() {
		MockitoAnnotations.initMocks(this);
		this.service = new CentreGestionService(centreGestionRepository);

		final NiveauCentre niveauCentre = new NiveauCentre();
		niveauCentre.setLabel("libel");
		niveauCentre.setTemEnServ("A");

		final Confidentialite confidentialite = new Confidentialite();
		confidentialite.setCode("A");
		confidentialite.setLabel("libel");
		confidentialite.setTemEnServ("A");

		final CentreGestion centreGestion = new CentreGestion();
		centreGestion.setId(1);
		centreGestion.setMail("centre.gestion@esup.fr");
		centreGestion.setConfidentialite(confidentialite);
		centreGestion.setNiveauCentre(niveauCentre);

		when(centreGestionRepository.findAll(Mockito.any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(centreGestion)));
	}

	@Test
	@DisplayName("findAll – Nominal test case")
	void findAll01() {
		final Page<CentreGestionBean> result = service.findAll(Pageable.unpaged());
		assertFalse(result.isEmpty());

		final Iterator<CentreGestionBean> iterator = result.getContent().iterator();
		assertTrue(iterator.hasNext());

		final CentreGestionBean bean = iterator.next();
		assertNotNull(bean);
		assertEquals(1, bean.getId());
		assertEquals("centre.gestion@esup.fr", bean.getMail());

		assertFalse(iterator.hasNext());
	}

}
