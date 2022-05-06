package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "ConventionNomenclature")
public class ConventionNomenclature {
    @Id
    @Column(nullable = false)
    private int id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "idConvention", unique = true)
    private Convention convention;

    @Column(length = 100)
    private String langueConvention;

    @Column(length = 50)
    private String devise;

    @Column(length = 150)
    private String modeValidationStage;

    @Column(length = 50)
    private String modeVersGratification;

    @Column(length = 150)
    private String natureTravail;

    @Column(length = 45)
    private String origineStage;

    @Column(length = 200)
    private String tempsTravail;

    @Column(length = 50)
    private String theme;

    @Column(length = 60)
    private String typeConvention;

    @Column(length = 100)
    private String uniteDureeExceptionnelle;

    @Column(length = 100)
    private String uniteDureeGratification;

    @Column(length = 50)
    private String uniteGratification;

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

    public String getLangueConvention() {
        return langueConvention;
    }

    public void setLangueConvention(String langueConvention) {
        this.langueConvention = langueConvention;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getModeValidationStage() {
        return modeValidationStage;
    }

    public void setModeValidationStage(String modeValidationStage) {
        this.modeValidationStage = modeValidationStage;
    }

    public String getModeVersGratification() {
        return modeVersGratification;
    }

    public void setModeVersGratification(String modeVersGratification) {
        this.modeVersGratification = modeVersGratification;
    }

    public String getNatureTravail() {
        return natureTravail;
    }

    public void setNatureTravail(String natureTravail) {
        this.natureTravail = natureTravail;
    }

    public String getOrigineStage() {
        return origineStage;
    }

    public void setOrigineStage(String origineStage) {
        this.origineStage = origineStage;
    }

    public String getTempsTravail() {
        return tempsTravail;
    }

    public void setTempsTravail(String tempsTravail) {
        this.tempsTravail = tempsTravail;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getTypeConvention() {
        return typeConvention;
    }

    public void setTypeConvention(String typeConvention) {
        this.typeConvention = typeConvention;
    }

    public String getUniteDureeExceptionnelle() {
        return uniteDureeExceptionnelle;
    }

    public void setUniteDureeExceptionnelle(String uniteDureeExceptionnelle) {
        this.uniteDureeExceptionnelle = uniteDureeExceptionnelle;
    }

    public String getUniteDureeGratification() {
        return uniteDureeGratification;
    }

    public void setUniteDureeGratification(String uniteDureeGratification) {
        this.uniteDureeGratification = uniteDureeGratification;
    }

    public String getUniteGratification() {
        return uniteGratification;
    }

    public void setUniteGratification(String uniteGratification) {
        this.uniteGratification = uniteGratification;
    }
}
