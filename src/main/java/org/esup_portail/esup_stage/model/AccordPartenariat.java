package org.esup_portail.esup_stage.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "AccordPartenariat")
public class AccordPartenariat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAccordPartenariat", nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "idStructure", nullable = false)
    private Structure structure;

    @ManyToOne
    @JoinColumn(name = "idContact", nullable = false)
    private Contact contact;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValidation;

    @Column(length = 50)
    private String loginValidation;

    @Column(nullable = false)
    private boolean estValide;

    @Column(nullable = false)
    private boolean comptesSupprimes;

    @Lob
    private String raisonSuppressionComptes;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSuppressionComptes;

    @Column(length = 50)
    private String loginSuppressionComptes;

    @Column(nullable = false, length = 50)
    private String loginCreation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date dateCreation;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
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

    public boolean isEstValide() {
        return estValide;
    }

    public void setEstValide(boolean estValide) {
        this.estValide = estValide;
    }

    public boolean isComptesSupprimes() {
        return comptesSupprimes;
    }

    public void setComptesSupprimes(boolean comptesSupprimes) {
        this.comptesSupprimes = comptesSupprimes;
    }

    public String getRaisonSuppressionComptes() {
        return raisonSuppressionComptes;
    }

    public void setRaisonSuppressionComptes(String raisonSuppressionComptes) {
        this.raisonSuppressionComptes = raisonSuppressionComptes;
    }

    public Date getDateSuppressionComptes() {
        return dateSuppressionComptes;
    }

    public void setDateSuppressionComptes(Date dateSuppressionComptes) {
        this.dateSuppressionComptes = dateSuppressionComptes;
    }

    public String getLoginSuppressionComptes() {
        return loginSuppressionComptes;
    }

    public void setLoginSuppressionComptes(String loginSuppressionComptes) {
        this.loginSuppressionComptes = loginSuppressionComptes;
    }

    public String getLoginCreation() {
        return loginCreation;
    }

    public void setLoginCreation(String loginCreation) {
        this.loginCreation = loginCreation;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }
}
