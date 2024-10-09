package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import jakarta.persistence.*;

@Entity
@Table(name = "NAF_N5")
public class NafN5 {

    @JsonView(Views.List.class)
    @Id
    @Column(name = "codeNAF_N5", nullable = false)
    private String code;

    @JsonView(Views.List.class)
    @Column(name = "libelleNAF_N5")
    private String libelle;

    @Column(name = "temEnServNAF_N5", length = 1)
    private String temEnServ;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "codeNAF_N1", nullable = false)
    private NafN1 nafN1;

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

    public NafN1 getNafN1() {
        return nafN1;
    }

    public void setNafN1(NafN1 nafN1) {
        this.nafN1 = nafN1;
    }
}
