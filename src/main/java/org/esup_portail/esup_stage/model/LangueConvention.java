package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LangueConvention")
public class LangueConvention implements Exportable {

    @Id
    @Column(name = "codeLangueConvention", nullable = false)
    private String code;

    @Column(name = "libelleLangueConvention", nullable = false)
    private String libelle;

    @Column(name = "temEnServLangue", length = 1)
    private String temEnServ;

    @JsonIgnore
    @OneToMany(mappedBy = "langueConvention")
    private List<TemplateConvention> templates = new ArrayList<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getTemEnServ() {
        return temEnServ;
    }

    public void setTemEnServ(String temEnServ) {
        this.temEnServ = temEnServ;
    }

    public List<TemplateConvention> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateConvention> templates) {
        this.templates = templates;
    }

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "libelle":
                value = getLibelle();
                break;
            case "actif":
                value = getTemEnServ().equals("O") ? "Oui" : "Non";
                break;
            default:
                break;
        }
        return value;
    }
}
