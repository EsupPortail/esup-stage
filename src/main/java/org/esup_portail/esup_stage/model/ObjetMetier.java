package org.esup_portail.esup_stage.model;

import org.esup_portail.esup_stage.security.ServiceContext;

import javax.persistence.*;
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

    @PrePersist
    public void prePersist() {
        setDateCreation(new Date());
        if (getLoginCreation() == null) {
            setLoginCreation(getUtilisateurLogin());
        }
    }

    @PreUpdate
    public void preUpdate() {
        setDateModif(new Date());
        setLoginModif(getUtilisateurLogin());
    }

    private static String getUtilisateurLogin() {
        if (ServiceContext.getServiceContext()!=null) {
            return ServiceContext.getServiceContext().getUtilisateur().getLogin();
        } else {
            return null;
        }
    }
}
