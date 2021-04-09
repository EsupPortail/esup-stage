package fr.esupportail.esupstage.controllers.jsf.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import fr.esupportail.esupstage.domain.jpa.entities.Effectif;
import fr.esupportail.esupstage.domain.jpa.entities.Pays;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.StructureRepository;
import fr.esupportail.esupstage.services.EffectifService;
import fr.esupportail.esupstage.services.PaysService;
import fr.esupportail.esupstage.services.TypeStructureService;
import fr.esupportail.esupstage.services.beans.EffectifBean;
import fr.esupportail.esupstage.services.beans.PaysBean;
import fr.esupportail.esupstage.services.beans.TypeStructureBean;
import lombok.Getter;
import lombok.Setter;

@Named("structureCreationView")
@ViewScoped
@Getter
@Setter
public class StructureCreationView implements Serializable {

	private String raisonSociale;

	private String numeroSiret;

	private Effectif effectif;

	private TypeStructure typeStructure;

	private String codeAPE;

	private String activite;

	private String voie;

	private String codePostal;

	private String batiment;

	private String commune;

	private String cedex;

	private Pays pays;

	private String email;

	private String phone;

	private String website;

	private String fax;

	private Structure structure;

	@Autowired
	private EffectifService effectifService;

	@Autowired
	private TypeStructureService typeStructureService;

	@Autowired
	private PaysService paysService;

	@Autowired
	private StructureRepository structureRepository;

	private Map<String, Effectif> effectifs;

	private Map<String, TypeStructure> typesStructure;

	private Map<String, Pays> paysList;

	@PostConstruct
	public void init() {
		// TEMPO

		effectifs = new HashMap<>();
		for (EffectifBean effectifBean : effectifService.findAll(PageRequest.of(0, Integer.MAX_VALUE))) {
			effectifs.put(effectifBean.getLibelleEffectif(), EffectifService.convert(effectifBean));
		}

		typesStructure = new HashMap<>();
		for (TypeStructureBean typeStructureBean : typeStructureService.findAll(PageRequest.of(0, Integer.MAX_VALUE))) {
			typesStructure.put(typeStructureBean.getLibelleTypeStructure(),
					TypeStructureService.convert(typeStructureBean));
		}

		paysList = new HashMap<>();
		for (PaysBean paysBean : paysService.findAll(PageRequest.of(0, Integer.MAX_VALUE))) {
			paysList.put(paysBean.getLib(), PaysService.convert(paysBean));
		}
	}

	public void submitForm() {

		Structure structure = new Structure();
		structure.setRaisonSociale(this.raisonSociale);
		structure.setNumeroSiret(this.numeroSiret);
		structure.setEffectif(this.effectif);
		structure.setTypeStructure(this.typeStructure);
		structure.setActivitePrincipale(this.activite);
		structure.setVoie(this.voie);
		structure.setCodePostal(this.codePostal);
		structure.setBatimentResidence(this.batiment);
		structure.setLibCedex(this.cedex);
		structure.setCommune(this.commune);
		structure.setPay(this.pays);
		structure.setMail(this.email);
		structure.setTelephone(this.phone);
		structure.setSiteWeb(this.website);
		structure.setFax(this.fax);

		structureRepository.save(structure);

		System.out.println(structure);
	}

}
