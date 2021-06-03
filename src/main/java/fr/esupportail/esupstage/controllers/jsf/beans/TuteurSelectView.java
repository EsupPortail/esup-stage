package fr.esupportail.esupstage.controllers.jsf.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import fr.esupportail.esupstage.domain.jpa.entities.Effectif;
import fr.esupportail.esupstage.domain.jpa.entities.Pays;
import fr.esupportail.esupstage.domain.jpa.entities.Service;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.StructureRepository;
import fr.esupportail.esupstage.services.ContactService;
import fr.esupportail.esupstage.services.EffectifService;
import fr.esupportail.esupstage.services.PaysService;
import fr.esupportail.esupstage.services.ServiceService;
import fr.esupportail.esupstage.services.TypeStructureService;
import fr.esupportail.esupstage.services.beans.ContactBean;
import fr.esupportail.esupstage.services.beans.EffectifBean;
import fr.esupportail.esupstage.services.beans.PaysBean;
import fr.esupportail.esupstage.services.beans.ServiceBean;
import fr.esupportail.esupstage.services.beans.TypeStructureBean;
import fr.esupportail.esupstage.services.conventions.ConventionBean;
import lombok.Getter;
import lombok.Setter;

@Named("tuteurSelectView")
@ViewScoped
@Getter
@Setter
public class TuteurSelectView implements Serializable {

	private ContactBean tuteur;
	private Map<String, ContactBean> contacts;

	@Autowired
	private ContactService contactService;




	@PostConstruct
	public void init() {
		// TEMPO

		for (ContactBean contactBean: contactService.findAll(PageRequest.of(0, Integer.MAX_VALUE))) {
			contacts.put(contactBean.toString(),contactBean);
		}

	}

	public void submitForm() {

		ContactBean tuteur = new ContactBean();


	}

}
