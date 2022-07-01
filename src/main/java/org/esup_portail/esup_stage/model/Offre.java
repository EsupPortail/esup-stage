package org.esup_portail.esup_stage.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Offre")
public class Offre extends ObjetMetier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOffre", nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "idTypeOffre", nullable = false)
    private TypeOffre typeOffre;

    @ManyToOne
    @JoinColumn(name = "idContratOffre")
    private ContratOffre contratOffre;

    @Column
    private String moisDebut;

    @Column
    private String anneeDebut;

    @Column
    private String precisionDebut;

    private Integer duree;

    @ManyToOne
    @JoinColumn(name = "idUniteDuree")
    private UniteDuree uniteDuree;

    @ManyToOne
    @JoinColumn(name = "idQualificationSimplifiee")
    private FapQualificationSimplifiee qualificationSimplifiee;

    @ManyToOne
    @JoinColumn(name = "codeFAP_N3")
    private FapN3 fapN3;

    @Column(nullable = false)
    private String intitule;

    @Column(nullable = false)
    private String description;

    @Column
    private String lieuCommune;

    @Column
    private String codeCommune;

    @Column
    private String lieuCodePostal;

    @ManyToOne
    @JoinColumn(name = "idLieuPays")
    private Pays lieuPays;

    private boolean deplacement;
    private boolean permis;
    private boolean voiture;
    private boolean remuneration;

    @Column
    private String avantages;

    @Column
    private String precisionRemuneration;

    @ManyToOne
    @JoinColumn(name = "idTempsTravail")
    private TempsTravail tempsTravail;

    private Integer quotiteTravail;

    @Column
    private String commentaireTempsTravail;

    private String observations;

    @ManyToOne
    @JoinColumn(name = "idNiveauFormation")
    private NiveauFormation niveauFormation;

    private String competences;

    @Column
    private String referenceOffreEtablissement;

    @Temporal(TemporalType.DATE)
    private Date dateDiffusion;

    @Temporal(TemporalType.DATE)
    private Date dateFinDiffusion;

    @Column
    private String loginDiffusion;

    @Column
    private String loginStopDiffusion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateStopDiffusion;

    @ManyToOne
    @JoinColumn(name = "idStructure", nullable = false)
    private Structure structure;

    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;

    @ManyToOne
    @JoinColumn(name = "idReferent")
    private Contact referent;

    @ManyToOne
    @JoinColumn(name = "idContactCand")
    private Contact contactCand;

    @ManyToOne
    @JoinColumn(name = "idContactInfo")
    private Contact contactInfo;

    @ManyToOne
    @JoinColumn(name = "idContactProprio")
    private Contact contactProprio;

    @Column(nullable = false)
    private boolean cacherTelContactCand;

    @Column(nullable = false)
    private boolean cacherTelContactInfo;

    @Column(nullable = false)
    private boolean cacherFaxContactCand;

    @Column(nullable = false)
    private boolean cacherFaxContactInfo;

    @Column(nullable = false)
    private boolean cacherMailContactCand;

    @Column(nullable = false)
    private boolean cacherMailContactInfo;

    @Column(nullable = false)
    private boolean cacherNomContactCand;

    @Column(nullable = false)
    private boolean cacherNomContactInfo;

    @Column(nullable = false)
    private boolean cacherEtablissement;

    @Column(nullable = false)
    private boolean estPourvue;

    @Column(nullable = false)
    private boolean offrePourvueEtudiantLocal;

    @Column(nullable = false)
    private boolean estDiffusee;

    @Column(nullable = false)
    private boolean estValidee;

    /**
     * Etat de la validation (si validation des offres)
     * 0 : non validee
     * 1 : validee
     * 2 : en cours
     * 3 : refusee
     */
    @Column(nullable = false)
    private int etatValidation;


    @Column(nullable = false)
    private boolean estSupprimee;

    @Column(nullable = false)
    private boolean estAccessERQTH;

    @Column(nullable = false)
    private boolean estPrioERQTH;

    @Column
    private String precisionHandicap;

    @Column(nullable = false)
    private boolean avecFichier;

    @Column(nullable = false)
    private boolean avecLien;

    @Column
    private String lienAttache;

    @ManyToOne
    @JoinColumn(name = "idFichier")
    private Fichier fichier;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValidation;

    @Column
    private String loginValidation;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateStopValidation;

    @Column
    private String loginStopValidation;

    @Column
    private String loginRejetValidation;

    @Column(nullable = false)
    private String anneeUniversitaire;

    @ManyToMany
    @JoinTable(
            name = "OffreModeCandidature",
            joinColumns = @JoinColumn(name = "idOffre"),
            inverseJoinColumns = @JoinColumn(name = "idModeCandidature")
    )
    private List<ModeCandidature> modeCandidatures = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TypeOffre getTypeOffre() {
        return typeOffre;
    }

    public void setTypeOffre(TypeOffre typeOffre) {
        this.typeOffre = typeOffre;
    }

    public ContratOffre getContratOffre() {
        return contratOffre;
    }

    public void setContratOffre(ContratOffre contratOffre) {
        this.contratOffre = contratOffre;
    }

    public String getMoisDebut() {
        return moisDebut;
    }

    public void setMoisDebut(String moisDebut) {
        this.moisDebut = moisDebut;
    }

    public String getAnneeDebut() {
        return anneeDebut;
    }

    public void setAnneeDebut(String anneeDebut) {
        this.anneeDebut = anneeDebut;
    }

    public String getPrecisionDebut() {
        return precisionDebut;
    }

    public void setPrecisionDebut(String precisionDebut) {
        this.precisionDebut = precisionDebut;
    }

    public Integer getDuree() {
        return duree;
    }

    public void setDuree(Integer duree) {
        this.duree = duree;
    }

    public UniteDuree getUniteDuree() {
        return uniteDuree;
    }

    public void setUniteDuree(UniteDuree uniteDuree) {
        this.uniteDuree = uniteDuree;
    }

    public FapQualificationSimplifiee getQualificationSimplifiee() {
        return qualificationSimplifiee;
    }

    public void setQualificationSimplifiee(FapQualificationSimplifiee qualificationSimplifiee) {
        this.qualificationSimplifiee = qualificationSimplifiee;
    }

    public FapN3 getFapN3() {
        return fapN3;
    }

    public void setFapN3(FapN3 fapN3) {
        this.fapN3 = fapN3;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLieuCommune() {
        return lieuCommune;
    }

    public void setLieuCommune(String lieuCommune) {
        this.lieuCommune = lieuCommune;
    }

    public String getCodeCommune() {
        return codeCommune;
    }

    public void setCodeCommune(String codeCommune) {
        this.codeCommune = codeCommune;
    }

    public String getLieuCodePostal() {
        return lieuCodePostal;
    }

    public void setLieuCodePostal(String lieuCodePostal) {
        this.lieuCodePostal = lieuCodePostal;
    }

    public Pays getLieuPays() {
        return lieuPays;
    }

    public void setLieuPays(Pays lieuPays) {
        this.lieuPays = lieuPays;
    }

    public boolean isDeplacement() {
        return deplacement;
    }

    public void setDeplacement(boolean deplacement) {
        this.deplacement = deplacement;
    }

    public boolean isPermis() {
        return permis;
    }

    public void setPermis(boolean permis) {
        this.permis = permis;
    }

    public boolean isVoiture() {
        return voiture;
    }

    public void setVoiture(boolean voiture) {
        this.voiture = voiture;
    }

    public boolean isRemuneration() {
        return remuneration;
    }

    public void setRemuneration(boolean remuneration) {
        this.remuneration = remuneration;
    }

    public String getAvantages() {
        return avantages;
    }

    public void setAvantages(String avantages) {
        this.avantages = avantages;
    }

    public String getPrecisionRemuneration() {
        return precisionRemuneration;
    }

    public void setPrecisionRemuneration(String precisionRemuneration) {
        this.precisionRemuneration = precisionRemuneration;
    }

    public TempsTravail getTempsTravail() {
        return tempsTravail;
    }

    public void setTempsTravail(TempsTravail tempsTravail) {
        this.tempsTravail = tempsTravail;
    }

    public Integer getQuotiteTravail() {
        return quotiteTravail;
    }

    public void setQuotiteTravail(Integer quotiteTravail) {
        this.quotiteTravail = quotiteTravail;
    }

    public String getCommentaireTempsTravail() {
        return commentaireTempsTravail;
    }

    public void setCommentaireTempsTravail(String commentaireTempsTravail) {
        this.commentaireTempsTravail = commentaireTempsTravail;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public NiveauFormation getNiveauFormation() {
        return niveauFormation;
    }

    public void setNiveauFormation(NiveauFormation niveauFormation) {
        this.niveauFormation = niveauFormation;
    }

    public String getCompetences() {
        return competences;
    }

    public void setCompetences(String competences) {
        this.competences = competences;
    }

    public String getReferenceOffreEtablissement() {
        return referenceOffreEtablissement;
    }

    public void setReferenceOffreEtablissement(String referenceOffreEtablissement) {
        this.referenceOffreEtablissement = referenceOffreEtablissement;
    }

    public Date getDateDiffusion() {
        return dateDiffusion;
    }

    public void setDateDiffusion(Date dateDiffusion) {
        this.dateDiffusion = dateDiffusion;
    }

    public Date getDateFinDiffusion() {
        return dateFinDiffusion;
    }

    public void setDateFinDiffusion(Date dateFinDiffusion) {
        this.dateFinDiffusion = dateFinDiffusion;
    }

    public String getLoginDiffusion() {
        return loginDiffusion;
    }

    public void setLoginDiffusion(String loginDiffusion) {
        this.loginDiffusion = loginDiffusion;
    }

    public String getLoginStopDiffusion() {
        return loginStopDiffusion;
    }

    public void setLoginStopDiffusion(String loginStopDiffusion) {
        this.loginStopDiffusion = loginStopDiffusion;
    }

    public Date getDateStopDiffusion() {
        return dateStopDiffusion;
    }

    public void setDateStopDiffusion(Date dateStopDiffusion) {
        this.dateStopDiffusion = dateStopDiffusion;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    public CentreGestion getCentreGestion() {
        return centreGestion;
    }

    public void setCentreGestion(CentreGestion centreGestion) {
        this.centreGestion = centreGestion;
    }

    public Contact getReferent() {
        return referent;
    }

    public void setReferent(Contact referent) {
        this.referent = referent;
    }

    public Contact getContactCand() {
        return contactCand;
    }

    public void setContactCand(Contact contactCand) {
        this.contactCand = contactCand;
    }

    public Contact getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(Contact contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Contact getContactProprio() {
        return contactProprio;
    }

    public void setContactProprio(Contact contactProprio) {
        this.contactProprio = contactProprio;
    }

    public boolean isCacherTelContactCand() {
        return cacherTelContactCand;
    }

    public void setCacherTelContactCand(boolean cacherTelContactCand) {
        this.cacherTelContactCand = cacherTelContactCand;
    }

    public boolean isCacherTelContactInfo() {
        return cacherTelContactInfo;
    }

    public void setCacherTelContactInfo(boolean cacherTelContactInfo) {
        this.cacherTelContactInfo = cacherTelContactInfo;
    }

    public boolean isCacherFaxContactCand() {
        return cacherFaxContactCand;
    }

    public void setCacherFaxContactCand(boolean cacherFaxContactCand) {
        this.cacherFaxContactCand = cacherFaxContactCand;
    }

    public boolean isCacherFaxContactInfo() {
        return cacherFaxContactInfo;
    }

    public void setCacherFaxContactInfo(boolean cacherFaxContactInfo) {
        this.cacherFaxContactInfo = cacherFaxContactInfo;
    }

    public boolean isCacherMailContactCand() {
        return cacherMailContactCand;
    }

    public void setCacherMailContactCand(boolean cacherMailContactCand) {
        this.cacherMailContactCand = cacherMailContactCand;
    }

    public boolean isCacherMailContactInfo() {
        return cacherMailContactInfo;
    }

    public void setCacherMailContactInfo(boolean cacherMailContactInfo) {
        this.cacherMailContactInfo = cacherMailContactInfo;
    }

    public boolean isCacherNomContactCand() {
        return cacherNomContactCand;
    }

    public void setCacherNomContactCand(boolean cacherNomContactCand) {
        this.cacherNomContactCand = cacherNomContactCand;
    }

    public boolean isCacherNomContactInfo() {
        return cacherNomContactInfo;
    }

    public void setCacherNomContactInfo(boolean cacherNomContactInfo) {
        this.cacherNomContactInfo = cacherNomContactInfo;
    }

    public boolean isCacherEtablissement() {
        return cacherEtablissement;
    }

    public void setCacherEtablissement(boolean cacherEtablissement) {
        this.cacherEtablissement = cacherEtablissement;
    }

    public boolean isEstPourvue() {
        return estPourvue;
    }

    public void setEstPourvue(boolean estPourvue) {
        this.estPourvue = estPourvue;
    }

    public boolean isOffrePourvueEtudiantLocal() {
        return offrePourvueEtudiantLocal;
    }

    public void setOffrePourvueEtudiantLocal(boolean offrePourvueEtudiantLocal) {
        this.offrePourvueEtudiantLocal = offrePourvueEtudiantLocal;
    }

    public boolean isEstDiffusee() {
        return estDiffusee;
    }

    public void setEstDiffusee(boolean estDiffusee) {
        this.estDiffusee = estDiffusee;
    }

    public boolean isEstValidee() {
        return estValidee;
    }

    public void setEstValidee(boolean estValidee) {
        this.estValidee = estValidee;
    }

    public int getEtatValidation() {
        return etatValidation;
    }

    public void setEtatValidation(int etatValidation) {
        this.etatValidation = etatValidation;
    }

    public boolean isEstSupprimee() {
        return estSupprimee;
    }

    public void setEstSupprimee(boolean estSupprimee) {
        this.estSupprimee = estSupprimee;
    }

    public boolean isEstAccessERQTH() {
        return estAccessERQTH;
    }

    public void setEstAccessERQTH(boolean estAccessERQTH) {
        this.estAccessERQTH = estAccessERQTH;
    }

    public boolean isEstPrioERQTH() {
        return estPrioERQTH;
    }

    public void setEstPrioERQTH(boolean estPrioERQTH) {
        this.estPrioERQTH = estPrioERQTH;
    }

    public String getPrecisionHandicap() {
        return precisionHandicap;
    }

    public void setPrecisionHandicap(String precisionHandicap) {
        this.precisionHandicap = precisionHandicap;
    }

    public boolean isAvecFichier() {
        return avecFichier;
    }

    public void setAvecFichier(boolean avecFichier) {
        this.avecFichier = avecFichier;
    }

    public boolean isAvecLien() {
        return avecLien;
    }

    public void setAvecLien(boolean avecLien) {
        this.avecLien = avecLien;
    }

    public String getLienAttache() {
        return lienAttache;
    }

    public void setLienAttache(String lienAttache) {
        this.lienAttache = lienAttache;
    }

    public Fichier getFichier() {
        return fichier;
    }

    public void setFichier(Fichier fichier) {
        this.fichier = fichier;
    }

    public Date getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(Date dateValidation) {
        this.dateValidation = dateValidation;
    }

    public String getLoginValidation() {
        return loginValidation;
    }

    public void setLoginValidation(String loginValidation) {
        this.loginValidation = loginValidation;
    }

    public Date getDateStopValidation() {
        return dateStopValidation;
    }

    public void setDateStopValidation(Date dateStopValidation) {
        this.dateStopValidation = dateStopValidation;
    }

    public String getLoginStopValidation() {
        return loginStopValidation;
    }

    public void setLoginStopValidation(String loginStopValidation) {
        this.loginStopValidation = loginStopValidation;
    }

    public String getLoginRejetValidation() {
        return loginRejetValidation;
    }

    public void setLoginRejetValidation(String loginRejetValidation) {
        this.loginRejetValidation = loginRejetValidation;
    }

    public String getAnneeUniversitaire() {
        return anneeUniversitaire;
    }

    public void setAnneeUniversitaire(String anneeUniversitaire) {
        this.anneeUniversitaire = anneeUniversitaire;
    }

    public List<ModeCandidature> getModeCandidatures() {
        return modeCandidatures;
    }

    public void setModeCandidatures(List<ModeCandidature> modeCandidatures) {
        this.modeCandidatures = modeCandidatures;
    }
}
