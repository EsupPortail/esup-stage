package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "NAF_N1")
public class NafN1 implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @Column(name = "codeNAF_N1", nullable = false)
    private String code;

    @JsonView(Views.List.class)
    @Column(name = "libelleNAF_N1", nullable = false, length = 150)
    private String libelle;

    @Column(name = "temEnServNAF_N1", length = 1)
    private String temEnServ;

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

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
