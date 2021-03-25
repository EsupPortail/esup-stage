package fr.esupportail.esupstage.controllers.jsf.beans;

import fr.esupportail.esupstage.domain.jpa.entities.*;
import fr.esupportail.esupstage.domain.jpa.repositories.StructureRepository;
import fr.esupportail.esupstage.services.EffectifService;
import fr.esupportail.esupstage.services.PaysService;
import fr.esupportail.esupstage.services.StructureService;
import fr.esupportail.esupstage.services.TypeStructureService;
import fr.esupportail.esupstage.services.beans.EffectifBean;
import fr.esupportail.esupstage.services.beans.PaysBean;
import fr.esupportail.esupstage.services.beans.TypeStructureBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named("structureCreationView")
@ViewScoped
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


		@Inject
		private EffectifService effectifService;
		@Inject
		private TypeStructureService typeStructureService;
		@Inject
		private PaysService paysService;
		@Inject
		private StructureRepository structureRepository;

		private Map<String,Effectif> effectifs;
		private Map<String,TypeStructure> typesStructure;
		private Map<String,Pays> paysList;

		@PostConstruct
		public void init() {
				//TEMPO


				effectifs  = new HashMap<>();
				for(EffectifBean effectifBean : effectifService.findAll(PageRequest.of(0, Integer.MAX_VALUE))){
						effectifs.put(effectifBean.getLibelleEffectif(),EffectifService.convert(effectifBean));
				}

				typesStructure = new HashMap<>();
				for(TypeStructureBean typeStructureBean : typeStructureService.findAll(PageRequest.of(0, Integer.MAX_VALUE))){
						typesStructure.put(typeStructureBean.getLibelleTypeStructure(),TypeStructureService.convert(typeStructureBean));
				}

				paysList = new HashMap<>();
				for(PaysBean paysBean : paysService.findAll(PageRequest.of(0, Integer.MAX_VALUE))){
						paysList.put(paysBean.getLib(),PaysService.convert(paysBean));
				}
		}

		public Effectif getEffectif() {
				return effectif;
		}

		public void setEffectif(Effectif effectif) {
				this.effectif = effectif;
		}

		public String getRaisonSociale() {
				return raisonSociale;
		}

		public void setRaisonSociale(String raisonSociale) {
				this.raisonSociale = raisonSociale;
		}

		public String getNumeroSiret() {
				return numeroSiret;
		}

		public void setNumeroSiret(String numeroSiret) {
				this.numeroSiret = numeroSiret;
		}

		public void setStructure(Structure structure) {
				this.structure = structure;
		}

		public Map<String, Effectif> getEffectifs() {
				return effectifs;
		}

		public void setEffectifs(Map<String, Effectif> effectifs) {
				this.effectifs = effectifs;
		}

		public TypeStructure getTypeStructure() {
				return typeStructure;
		}

		public void setTypeStructure(TypeStructure typeStructure) {
				this.typeStructure = typeStructure;
		}

		public Map<String, TypeStructure> getTypesStructure() {
				return typesStructure;
		}

		public void setTypesStructure(Map<String, TypeStructure> typesStructure) {
				this.typesStructure = typesStructure;
		}

		public String getCodeAPE() {
				return codeAPE;
		}

		public void setCodeAPE(String codeAPE) {
				this.codeAPE = codeAPE;
		}

		public String getActivite() {
				return activite;
		}

		public void setActivite(String activite) {
				this.activite = activite;
		}

		public String getVoie() {
				return voie;
		}

		public void setVoie(String voie) {
				this.voie = voie;
		}

		public String getCodePostal() {
				return codePostal;
		}

		public void setCodePostal(String codePostal) {
				this.codePostal = codePostal;
		}

		public String getBatiment() {
				return batiment;
		}

		public void setBatiment(String batiment) {
				this.batiment = batiment;
		}

		public String getCommune() {
				return commune;
		}

		public void setCommune(String commune) {
				this.commune = commune;
		}

		public String getCedex() {
				return cedex;
		}

		public void setCedex(String cedex) {
				this.cedex = cedex;
		}

		public Pays getPays() {
				return pays;
		}

		public void setPays(Pays pays) {
				this.pays = pays;
		}

		public String getEmail() {
				return email;
		}

		public void setEmail(String email) {
				this.email = email;
		}

		public String getPhone() {
				return phone;
		}

		public void setPhone(String phone) {
				this.phone = phone;
		}

		public String getWebsite() {
				return website;
		}

		public void setWebsite(String website) {
				this.website = website;
		}

		public String getFax() {
				return fax;
		}

		public void setFax(String fax) {
				this.fax = fax;
		}

		public void submitForm(){
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

		private Structure structure;
		public Structure getStructure() {
				return this.structure;
		}

		public Map<String, Pays> getPaysList() {
				return paysList;
		}

		public void setPaysList(Map<String, Pays> paysList) {
				this.paysList = paysList;
		}
}
