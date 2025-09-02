package org.esup_portail.esup_stage.service.apogee.model;


import lombok.Data;

@Data
public class InfosAdmEtu {

    private String handicap;

    private String numEtu;
    private String codEtu;
    private String codInd;
    private String emailAnnuaire;
    private String loginAnnuaire;

    private String numBoursier;
    private String numeroINE;

    private String nomUsuel;
    private String prenom1;
    private String prenom2;
    private String nomPatronymique;

    private String prenomEtatCivil;
    private String sexEtatCivil;

    private String nationaliteDTO;

    private String dateNaissance;
    private String libVilleNaissance;
    private String departementNaissance;

    private String paysNaissance;

    private String sexe;
    private String situationFamiliale;
    private String situationMilitaire;

    private String listeBacs;
    private String listeBlocages;

    private String anneePremiereInscEnsSup;
    private String anneePremiereInscEtb;
    private String anneePremiereInscEtr;
    private String anneePremiereInscUniv;
    private String etbPremiereInscUniv;
    private String temoinDateNaissEstimee;
    private String temoinSitMilEnRegle;
    // libelle CPAM etudiant
    private String libelleCPAM = "";
}
