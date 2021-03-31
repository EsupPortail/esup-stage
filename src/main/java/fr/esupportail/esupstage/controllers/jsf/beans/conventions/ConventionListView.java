package fr.esupportail.esupstage.controllers.jsf.beans.conventions;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import fr.esupportail.esupstage.domain.jpa.entities.CentreGestion;
import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;
import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import fr.esupportail.esupstage.domain.jpa.entities.Etudiant;
import fr.esupportail.esupstage.domain.jpa.entities.Indemnisation;
import fr.esupportail.esupstage.domain.jpa.entities.LangueConvention;
import fr.esupportail.esupstage.domain.jpa.entities.ModeValidationStage;
import fr.esupportail.esupstage.domain.jpa.entities.NatureTravail;
import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;
import fr.esupportail.esupstage.domain.jpa.entities.TempsTravail;
import fr.esupportail.esupstage.domain.jpa.entities.Theme;
import fr.esupportail.esupstage.domain.jpa.entities.TypeConvention;
import fr.esupportail.esupstage.services.conventions.ConventionService;
import lombok.Getter;

@Named("conventionListView")
@ViewScoped
public class ConventionListView implements Serializable {

    private static final long serialVersionUID = 7987062852304528092L;

    @Getter private List<Convention> conventions = new ArrayList<Convention>(){
        private static final long serialVersionUID = -1736508108561524507L;
        {
			System.out.println("test");
        final NiveauCentre niveauCentre = new NiveauCentre();
		niveauCentre.setLibelleNiveauCentre("libel");
		niveauCentre.setTemEnServNiveauCentre("A");

		final Confidentialite confidentialite = new Confidentialite();
		confidentialite.setCodeConfidentialite("A");
		confidentialite.setLibelleConfidentialite("libel");
		confidentialite.setTemEnServConfid("A");

		final CentreGestion centreGestion = new CentreGestion();
		centreGestion.setAutorisationEtudiantCreationConvention(true);
		centreGestion.setCodeUniversite("codeuniv");
		centreGestion.setDateCreation(new Date());
		centreGestion.setIdModeValidationStage(1);
		centreGestion.setLoginCreation("login");
		centreGestion.setConfidentialite(confidentialite);
		centreGestion.setNiveauCentre(niveauCentre);

		final TypeConvention typeConvention = new TypeConvention();
		typeConvention.setCodeCtrl("code");
		typeConvention.setLibelleTypeConvention("libel");
		typeConvention.setTemEnServTypeConvention("F");

		final Theme theme = new Theme();
		theme.setLibelleTheme("libel");

		final TempsTravail tempsTravail = new TempsTravail();
		tempsTravail.setCodeCtrl("code");
		tempsTravail.setLibelleTempsTravail("libel");
		tempsTravail.setTemEnServTempsTravail("F");

		final NatureTravail natureTravail = new NatureTravail();
		natureTravail.setLibelleNatureTravail("libel");
		natureTravail.setTemEnServNatTrav("F");

		final ModeValidationStage modeValidationStage = new ModeValidationStage();
		modeValidationStage.setLibelleModeValidationStage("libel");
		modeValidationStage.setTemEnServModeValid("F");

		final LangueConvention langueConvention = new LangueConvention();
		langueConvention.setCodeLangueConvention("CD");
		langueConvention.setLibelleLangueConvention("libel");

		final Indemnisation indemnisation = new Indemnisation();
		indemnisation.setLibelleIndemnisation("indem");
		indemnisation.setTemEnServIndem("F");

		final Etudiant etudiant = new Etudiant();
		etudiant.setCodeUniversite("code");
		etudiant.setCreatedDate(LocalDateTime.now());
		etudiant.setIdentEtudiant("ident");
		etudiant.setCreatedBy("login");
		etudiant.setNom("Name");
		etudiant.setNumEtudiant("125458");
		etudiant.setPrenom("Firstname");

		final Convention convention = new Convention();
		convention.setDateCreation(new Date(0));
		convention.setDateDebutStage(new Date(0));
		convention.setDateFinStage(new Date(0));
		convention.setDureeStage(100);
		convention.setIdAssurance(1);
		convention.setIdModeVersGratification(1);
		convention.setLoginCreation("login");
		convention.setNbJoursHebdo("1");
		convention.setSujetStage("subject");
		convention.setTemConfSujetTeme("s");
		convention.setEtudiant(etudiant);
		convention.setIndemnisation(indemnisation);
		convention.setLangueConvention(langueConvention);
		convention.setModeValidationStage(modeValidationStage);
		convention.setNatureTravail(natureTravail);
		convention.setTempsTravail(tempsTravail);
		convention.setTheme(theme);
		convention.setTypeConvention(typeConvention);
		convention.setCentreGestion(centreGestion);

        this.add(convention);
    }};

    @Inject
    private ConventionService service;

    public List<Convention> getConventions() {
        return conventions;
    }

    public void setService(ConventionService service) {
        this.service = service;
    }
}
