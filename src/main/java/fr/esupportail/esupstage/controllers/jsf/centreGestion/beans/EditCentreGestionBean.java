package fr.esupportail.esupstage.controllers.jsf.centreGestion.beans;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EditCentreGestionBean implements Serializable {

	/**
	 * Serial
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;

	private String adresse;

	private String codePostal;

	private String commune;

	private String fax;

	private String mail;

	private String nomCentre;

	private String siteWeb;

	private String telephone;

	private String urlPageInstruction;

	private String voie;

	private Integer niveauCentre;

	private String confidentialite;

}
