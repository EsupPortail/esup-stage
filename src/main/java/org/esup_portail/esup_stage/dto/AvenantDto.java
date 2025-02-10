package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;

public class AvenantDto {

    @NotNull
    private int idConvention;

    @NotEmpty
    @NotNull
    private String titreAvenant;

    private String motifAvenant;

    @NotNull
    private boolean rupture;

    @NotNull
    private boolean modificationPeriode;

    private Date dateDebutStage;

    private Date dateFinStage;

    @NotNull
    private boolean interruptionStage;

    private Date dateDebutInterruption;

    private Date dateFinInterruption;

    private Boolean modificationLieu;

    private int idService;

    @NotNull
    private boolean modificationSujet;

    private String sujetStage;

    @NotNull
    private boolean modificationEnseignant;

    @NotNull
    private boolean modificationSalarie;

    private int idContact;

    @NotNull
    private boolean validationAvenant;

    private int idEnseignant;

    @Size(max = 7)
    private String montantGratification;

    private int idUniteGratification;

    private int idModeVersGratification;

    private int idDevise;

    @NotNull
    private boolean modificationMontantGratification;

    private Date dateRupture;

    private String commentaireRupture;

    @Size(max = 50)
    private String monnaieGratification;

    private int idUniteDuree;

    public int getIdConvention() {
        return idConvention;
    }

    public void setIdConvention(int idConvention) {
        this.idConvention = idConvention;
    }

    public String getTitreAvenant() {
        return titreAvenant;
    }

    public void setTitreAvenant(String titreAvenant) {
        this.titreAvenant = titreAvenant;
    }

    public String getMotifAvenant() {
        return motifAvenant;
    }

    public void setMotifAvenant(String motifAvenant) {
        this.motifAvenant = motifAvenant;
    }

    public boolean getRupture() {
        return rupture;
    }

    public void setRupture(boolean rupture) {
        this.rupture = rupture;
    }

    public boolean getModificationPeriode() {
        return modificationPeriode;
    }

    public void setModificationPeriode(boolean modificationPeriode) {
        this.modificationPeriode = modificationPeriode;
    }

    public Date getDateDebutStage() {
        return dateDebutStage;
    }

    public void setDateDebutStage(Date dateDebutStage) {
        this.dateDebutStage = dateDebutStage;
    }

    public Date getDateFinStage() {
        return dateFinStage;
    }

    public void setDateFinStage(Date dateFinStage) {
        this.dateFinStage = dateFinStage;
    }

    public boolean getInterruptionStage() {
        return interruptionStage;
    }

    public void setInterruptionStage(boolean interruptionStage) {
        this.interruptionStage = interruptionStage;
    }

    public Date getDateDebutInterruption() {
        return dateDebutInterruption;
    }

    public void setDateDebutInterruption(Date dateDebutInterruption) {
        this.dateDebutInterruption = dateDebutInterruption;
    }

    public Date getDateFinInterruption() {
        return dateFinInterruption;
    }

    public void setDateFinInterruption(Date dateFinInterruption) {
        this.dateFinInterruption = dateFinInterruption;
    }

    public Boolean getModificationLieu() {
        return modificationLieu;
    }

    public void setModificationLieu(Boolean modificationLieu) {
        this.modificationLieu = modificationLieu;
    }

    public int getIdService() {
        return idService;
    }

    public void setIdService(int idService) {
        this.idService = idService;
    }

    public boolean getModificationSujet() {
        return modificationSujet;
    }

    public void setModificationSujet(boolean modificationSujet) {
        this.modificationSujet = modificationSujet;
    }

    public String getSujetStage() {
        return sujetStage;
    }

    public void setSujetStage(String sujetStage) {
        this.sujetStage = sujetStage;
    }

    public boolean getModificationEnseignant() {
        return modificationEnseignant;
    }

    public void setModificationEnseignant(boolean modificationEnseignant) {
        this.modificationEnseignant = modificationEnseignant;
    }

    public boolean getModificationSalarie() {
        return modificationSalarie;
    }

    public void setModificationSalarie(boolean modificationSalarie) {
        this.modificationSalarie = modificationSalarie;
    }

    public int getIdContact() {
        return idContact;
    }

    public void setIdContact(int idContact) {
        this.idContact = idContact;
    }

    public boolean getValidationAvenant() {
        return validationAvenant;
    }

    public void setValidationAvenant(boolean validationAvenant) {
        this.validationAvenant = validationAvenant;
    }

    public int getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(int idEnseignant) {
        this.idEnseignant = idEnseignant;
    }

    public String getMontantGratification() {
        return montantGratification;
    }

    public void setMontantGratification(String montantGratification) {
        this.montantGratification = montantGratification;
    }

    public int getIdUniteGratification() {
        return idUniteGratification;
    }

    public void setIdUniteGratification(int idUniteGratification) {
        this.idUniteGratification = idUniteGratification;
    }

    public boolean getModificationMontantGratification() {
        return modificationMontantGratification;
    }

    public void setModificationMontantGratification(boolean modificationMontantGratification) {
        this.modificationMontantGratification = modificationMontantGratification;
    }

    public int getIdModeVersGratification() {
        return idModeVersGratification;
    }

    public void setIdModeVersGratification(int idModeVersGratification) {
        this.idModeVersGratification = idModeVersGratification;
    }

    public int getIdDevise() {
        return idDevise;
    }

    public void setIdDevise(int idDevise) {
        this.idDevise = idDevise;
    }

    public Date getDateRupture() {
        return dateRupture;
    }

    public void setDateRupture(Date dateRupture) {
        this.dateRupture = dateRupture;
    }

    public String getCommentaireRupture() {
        return commentaireRupture;
    }

    public void setCommentaireRupture(String commentaireRupture) {
        this.commentaireRupture = commentaireRupture;
    }

    public String getMonnaieGratification() {
        return monnaieGratification;
    }

    public void setMonnaieGratification(String monnaieGratification) {
        this.monnaieGratification = monnaieGratification;
    }

    public int getIdUniteDuree() {
        return idUniteDuree;
    }

    public void setIdUniteDuree(int idUniteDuree) {
        this.idUniteDuree = idUniteDuree;
    }
}