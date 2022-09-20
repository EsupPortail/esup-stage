package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Avenant")
public class Avenant extends ObjetMetier implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAvenant", nullable = false)
    private int id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idConvention", nullable = false)
    private Convention convention;

    @JsonView(Views.List.class)
    @Lob
    private String titreAvenant;

    @Lob
    private String motifAvenant;

    @Column(nullable = false)
    private boolean rupture;

    @Column(nullable = false)
    private boolean modificationPeriode;

    @Temporal(TemporalType.DATE)
    private Date dateDebutStage;

    @Temporal(TemporalType.DATE)
    private Date dateFinStage;

    @Column(nullable = false)
    private boolean interruptionStage;

    @Temporal(TemporalType.DATE)
    private Date dateDebutInterruption;

    @Temporal(TemporalType.DATE)
    private Date dateFinInterruption;

    private Boolean modificationLieu;

    @ManyToOne
    @JoinColumn(name = "idService")
    private Service service;

    @Column(nullable = false)
    private boolean modificationSujet;

    @Lob
    private String sujetStage;

    @Column(nullable = false)
    private boolean modificationEnseignant;

    @Column(nullable = false)
    private boolean modificationSalarie;

    @ManyToOne
    @JoinColumn(name = "idContact")
    private Contact contact;

    @Column(nullable = false)
    private boolean validationAvenant;

    @ManyToOne
    @JoinColumn(name = "idEnseignant")
    private Enseignant enseignant;

    @Column
    private String montantGratification;

    @ManyToOne
    @JoinColumn(name = "idUniteGratification")
    private UniteGratification uniteGratification;

    @ManyToOne
    @JoinColumn(name = "idModeVersGratification")
    private ModeVersGratification modeVersGratification;

    @ManyToOne
    @JoinColumn(name = "idDevise")
    private Devise devise;

    @Column(nullable = false)
    private boolean modificationMontantGratification;

    @Temporal(TemporalType.DATE)
    private Date dateRupture;

    @Lob
    private String commentaireRupture;

    @Column
    private String monnaieGratification;

    @ManyToOne
    @JoinColumn(name = "idUniteDureeGratification")
    private UniteDuree uniteDuree;

    @Transient
    private String listeMotifsAvenant;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getListeMotifsAvenant() {
        String listeMotifsAvenant = "";
        if(rupture)
            listeMotifsAvenant += " - Rupture de stage<br>";
        if(modificationSujet)
            listeMotifsAvenant += " - Modification du sujet de stage<br>";
        if(modificationPeriode)
            listeMotifsAvenant += " - Modification de la période de stage<br>";
        if(modificationMontantGratification)
            listeMotifsAvenant += " - Modification du montant de la gratification<br>";
        if(modificationLieu)
            listeMotifsAvenant += " - Modification du lieu de stage<br>";
        if(modificationSalarie)
            listeMotifsAvenant += " - Modification du tuteur professionnel<br>";
        if(modificationEnseignant)
            listeMotifsAvenant += " - Modification de l'enseignant référant<br>";
        if(motifAvenant != null && !motifAvenant.isEmpty())
            listeMotifsAvenant += " - Autre modification : '"+motifAvenant +"'<br>";
        return listeMotifsAvenant;
    }

    public void setMotifAvenant(String motifAvenant) {
        this.motifAvenant = motifAvenant;
    }

    public boolean isRupture() {
        return rupture;
    }

    public void setRupture(boolean rupture) {
        this.rupture = rupture;
    }

    public boolean isModificationPeriode() {
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

    public boolean isInterruptionStage() {
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

    public boolean isModificationSujet() {
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

    public boolean isModificationEnseignant() {
        return modificationEnseignant;
    }

    public void setModificationEnseignant(boolean modificationEnseignant) {
        this.modificationEnseignant = modificationEnseignant;
    }

    public boolean isModificationSalarie() {
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

    public boolean isValidationAvenant() {
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

    public ModeVersGratification getModeVersGratification() {
        return modeVersGratification;
    }

    public void setModeVersGratification(ModeVersGratification modeVersGratification) {
        this.modeVersGratification = modeVersGratification;
    }

    public Devise getDevise() {
        return devise;
    }

    public void setDevise(Devise devise) {
        this.devise = devise;
    }

    public boolean isModificationMontantGratification() {
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

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
