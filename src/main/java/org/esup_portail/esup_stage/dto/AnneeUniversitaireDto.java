package org.esup_portail.esup_stage.dto;

public class AnneeUniversitaireDto {
    private String annee;
    private String libelle;
    private boolean anneeEnCours = false;

    public AnneeUniversitaireDto() { }

    public AnneeUniversitaireDto(String annee, String libelle) {
        this.annee = annee;
        this.libelle = libelle;
    }

    public AnneeUniversitaireDto(String annee, String libelle, boolean anneeEnCours) {
        this.annee = annee;
        this.libelle = libelle;
        this.anneeEnCours = anneeEnCours;
    }

    public String getAnnee() {
        return annee;
    }

    public void setAnnee(String annee) {
        this.annee = annee;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public boolean isAnneeEnCours() {
        return anneeEnCours;
    }

    public void setAnneeEnCours(boolean anneeEnCours) {
        this.anneeEnCours = anneeEnCours;
    }
}
