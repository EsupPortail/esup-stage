package fr.esupportail.esupstage.controllers.jsf.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import fr.esupportail.esupstage.domain.jpa.entities.Effectif;
import fr.esupportail.esupstage.domain.jpa.entities.Pays;
import fr.esupportail.esupstage.domain.jpa.entities.Service;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.StructureRepository;
import fr.esupportail.esupstage.services.EffectifService;
import fr.esupportail.esupstage.services.PaysService;
import fr.esupportail.esupstage.services.ServiceService;
import fr.esupportail.esupstage.services.TypeStructureService;
import fr.esupportail.esupstage.services.beans.EffectifBean;
import fr.esupportail.esupstage.services.beans.PaysBean;
import fr.esupportail.esupstage.services.beans.ServiceBean;
import fr.esupportail.esupstage.services.beans.TypeStructureBean;
import fr.esupportail.esupstage.services.conventions.ConventionBean;
import lombok.Getter;
import lombok.Setter;

@Named("structureSelectView")
@ViewScoped
@Getter
@Setter
public class ServiceSelectView implements Serializable {

	private ServiceBean service;
	private Map<String, ServiceBean> services;

	@Autowired
	private ServiceService serviceService;




	@PostConstruct
	public void init() {
		// TEMPO
		this.services = new HashMap<>();

		for (ServiceBean serviceBean: serviceService.findAll(PageRequest.of(0, Integer.MAX_VALUE))) {
			services.put(serviceBean.getNom(), serviceBean);
		}

		ServiceBean sb = new ServiceBean();
		sb.setNom("Le service");
		services.put(sb.getNom(),sb);

		ServiceBean sb2 = new ServiceBean();
		sb2.setNom("Le service2");
		services.put(sb2.getNom(),sb2);

	}

	public void submitForm() {
		Service service = new Service();
	}

	public void openForm(){
		Map<String,Object> options = new HashMap<String, Object>();
		options.put("modal", true);
		PrimeFaces.current().dialog().openDynamic("card2", options, null);
	}

	public void handleServiceChange(){
		System.out.println(service);
	}

	public void confirm(){

	}
}
