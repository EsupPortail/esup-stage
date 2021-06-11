package fr.esupportail.esupstage.services.beans;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CentreGestionBean implements Serializable {

	/**
	 * Serial
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;

	private String adresse;

	private boolean autorisationEtudiantCreationConvention;

	private boolean autoriserImpressionConvention;

	private String codePostal;

	private String codeUniversite;

	private String commentaire;

	private String commune;

	private boolean depotAnonyme;

	private String fax;

	private Integer idModeValidationStage;

	private String mail;

	private String nomCentre;

	private String nomViseur;

	private String prenomViseur;

	private boolean presenceTuteurEns;

	private boolean presenceTuteurPro;

	private String qualiteViseur;

	private boolean saisieTuteurProParEtudiant;

	private String siteWeb;

	private String telephone;

	private String urlPageInstruction;

	private boolean validationPedagogique;

	private boolean visibiliteEvalPro;

	private String voie;

	private NiveauCentreBean niveauCentre;

	private ConfidentialiteBean confidentialite;

}
