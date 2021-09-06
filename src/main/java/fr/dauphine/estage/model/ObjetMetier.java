package fr.dauphine.estage.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@MappedSuperclass
public abstract class ObjetMetier {

    @Column(nullable = false, length = 50)
    private String loginCreation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date dateCreation;

    @Column(length = 50)
    private String loginModif;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date dateModif;

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

    public String getLoginModif() {
        return loginModif;
    }

    public void setLoginModif(String loginModif) {
        this.loginModif = loginModif;
    }

    public Date getDateModif() {
        return dateModif;
    }

    public void setDateModif(Date dateModif) {
        this.dateModif = dateModif;
    }
}
