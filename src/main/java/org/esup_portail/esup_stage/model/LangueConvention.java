package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LangueConvention")
public class LangueConvention {

    @Id
    @Column(name = "codeLangueConvention", nullable = false)
    private String code;

    @Column(name = "libelleLangueConvention", nullable = false, length = 100)
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
}
