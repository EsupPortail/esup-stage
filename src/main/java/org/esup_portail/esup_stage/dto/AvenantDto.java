package org.esup_portail.esup_stage.dto;

import org.esup_portail.esup_stage.model.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

public class AvenantDto {

    private Convention convention;
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
    private Service service;
    @NotNull
    private boolean modificationSujet;
    private String sujetStage;
    @NotNull
    private boolean modificationEnseignant;
    @NotNull
    private boolean modificationSalarie;
    private Contact contact;
    @NotNull
    private boolean validationAvenant;
    private Enseignant enseignant;
    @Size(max = 7)
    private String montantGratification;
    private UniteGratification uniteGratification;
    @NotNull
    private boolean modificationMontantGratification;
    private Date dateRupture;
    private String commentaireRupture;
    @Size(max = 50)
    private String monnaieGratification;
    private UniteDuree uniteDuree;

    public Convention getConvention() {
        return convention;
    }

    public void setConvention(Convention convention) {
        this.convention = convention;
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

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
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

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public boolean getValidationAvenant() {
        return validationAvenant;
    }

    public void setValidationAvenant(boolean validationAvenant) {
        this.validationAvenant = validationAvenant;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public String getMontantGratification() {
        return montantGratification;
    }

    public void setMontantGratification(String montantGratification) {
        this.montantGratification = montantGratification;
    }

    public UniteGratification getUniteGratification() {
        return uniteGratification;
    }

    public void setUniteGratification(UniteGratification uniteGratification) {
        this.uniteGratification = uniteGratification;
    }

    public boolean getModificationMontantGratification() {
        return modificationMontantGratification;
    }

    public void setModificationMontantGratification(boolean modificationMontantGratification) {
        this.modificationMontantGratification = modificationMontantGratification;
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

    public UniteDuree getUniteDuree() {
        return uniteDuree;
    }

    public void setUniteDuree(UniteDuree uniteDuree) {
        this.uniteDuree = uniteDuree;
    }
}