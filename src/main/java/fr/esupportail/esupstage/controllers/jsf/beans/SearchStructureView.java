package fr.esupportail.esupstage.controllers.jsf.beans;

import com.sun.faces.util.Json;
import fr.esupportail.esupstage.domain.jpa.entities.*;
import fr.esupportail.esupstage.domain.jpa.repositories.StructureRepository;
import fr.esupportail.esupstage.services.StructureService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Named("searchStructureView")
@ViewScoped
public class SearchStructureView implements Serializable {


		private List<Structure> structures;
		private Structure selectedStructure;
		private String inputSiret;
		private String inputRaisonSociale;
		private String inputDepartement;
		private OkHttpClient client;

		@Inject
		private StructureService service;

		@PostConstruct
		public void init() {
				structures = new ArrayList<>();
				//TODO : Correctly handle https
				client = getUnsafeOkHttpClient();
		}
		private static OkHttpClient getUnsafeOkHttpClient() {
				try {
						// Create a trust manager that does not validate certificate chains
						final TrustManager[] trustAllCerts = new TrustManager[] {
								new X509TrustManager() {
										@Override
										public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
										}

										@Override
										public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
										}

										@Override
										public java.security.cert.X509Certificate[] getAcceptedIssuers() {
												return new java.security.cert.X509Certificate[]{};
										}
								}
						};

						// Install the all-trusting trust manager
						final SSLContext sslContext = SSLContext.getInstance("SSL");
						sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
						// Create an ssl socket factory with our all-trusting manager
						final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

						OkHttpClient.Builder builder = new OkHttpClient.Builder();
						builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
						builder.hostnameVerifier((hostname, session) -> true);

						OkHttpClient okHttpClient = builder.build();
						return okHttpClient;
				} catch (Exception e) {
						throw new RuntimeException(e);
				}
		}

		public List<Structure> getStructures() {
				return structures;
		}


		public void setService(StructureService service) {
				this.service = service;
		}

		public Structure getSelectedStructure() {
				return selectedStructure;
		}

		public void setSelectedStructure(Structure selectedStructure) {
				this.selectedStructure = selectedStructure;
		}

		public void onRowSelect(SelectEvent<Structure> event) {
				FacesMessage msg = new FacesMessage("Product Selected", String.valueOf(event.getObject().getNumeroSiret()));
				FacesContext.getCurrentInstance().addMessage(null, msg);
		}

		public void onRowUnselect(UnselectEvent<Structure> event) {
				FacesMessage msg = new FacesMessage("Product Unselected", String.valueOf(event.getObject().getNumeroSiret()));
				FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		public void searchCompagny() throws IOException {

				this.structures = new ArrayList<>();

				StringBuilder sb = new StringBuilder();
				sb.append("https://entreprise.data.gouv.fr/api/sirene/v1/");
				if(!inputSiret.equals("")){
						sb.append("siret/" + inputSiret.trim());
				}
				else{
						sb.append("full_text/"+this.inputRaisonSociale.trim() + "?code_postal=" +  inputDepartement.trim());
				}
				System.out.println(sb.toString());

				Request request = new Request.Builder()
						.url(sb.toString())
						.build();

				Response response = client.newCall(request).execute();


				JSONObject json = new JSONObject(response.body().string());
				JSONArray etabs = new JSONArray();
				if(!json.has("total_results")){
						if(!json.has("suggestions")){
								etabs.put((JSONObject) json.get("etablissement"));
						}

				}
				else{
						etabs.putAll(((JSONArray) json.get("etablissement")));
				}

				for(Object obj : etabs){
						JSONObject etab = (JSONObject) obj;
						//JSONObject unite = (JSONObject) etab.get("unite_legale");


						final Pays pays = new Pays();
						pays.setActual(1);
						pays.setCog(1);
						pays.setLib("France");
						pays.setSiretObligatoire(true);
						pays.setTemEnServ("A");

						final Effectif effectif = new Effectif();
						effectif.setLabel("Label");
						effectif.setTemEnServ("A");

						final TypeStructure typeStructure = new TypeStructure();
						typeStructure.setTemEnServ("A");
						typeStructure.setSiretObligatoire(true);
						typeStructure.setLabel("Label");

						final Structure structure = new Structure();
						structure.setEstValidee(1);
						structure.setNumeroSiret(etab.getString("siret"));
						structure.setRaisonSociale(etab.optString("l1_normalisee")+" " + etab.optString("l2_normalisee"));
						//structure.setVoie(etab.optString("numero_voie","") + " " +etab.getString("type_voie") + " " + etab.getString("libelle_voie"));
						structure.setVoie(etab.optString("l4_normalisee"));
						structure.setCodePostal(etab.getString("code_postal"));
						structure.setCommune(etab.getString("libelle_commune"));
						StatutJuridique juridique = new StatutJuridique();
						juridique.setLabel(etab.optString("libelle_nature_juridique_entreprise"));
						structure.setStatutJuridique(juridique);
						structure.setActivitePrincipale( etab.optString("libelle_activite_principale")) ;
						structure.setPay(pays);
						structure.setEffectif(effectif);
						structure.setLibCedex(etab.optString("l6_declaree"));
						structure.setTypeStructure(typeStructure);
						structure.setId(new SecureRandom().nextInt(9999));

						structures.add(structure);
				}




		}

		public String getInputSiret() {
				return inputSiret;
		}

		public void setInputSiret(String input) {
				this.inputSiret = input;
		}

		public String getInputDepartement() {
				return inputDepartement;
		}

		public void setInputDepartement(String inputDepartement) {
				this.inputDepartement = inputDepartement;
		}

		public String getInputRaisonSociale() {
				return inputRaisonSociale;
		}

		public void setInputRaisonSociale(String inputRaisonSociale) {
				this.inputRaisonSociale = inputRaisonSociale;
		}
}

