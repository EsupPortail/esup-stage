package org.esup_portail.esup_stage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import org.esup_portail.esup_stage.security.ServiceContext;

import java.util.Date;

@Entity
@Table(name = "ConventionDocumentEtudiantHistorique")
@Data
public class ConventionDocumentEtudiantHistorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idConventionDocumentEtudiantHistorique", nullable = false)
    private int id;

    @Column(nullable = false)
    private int idConvention;

    @Column(nullable = false)
    private int idDocumentEtudiant;

    @Column(name = "nomDocument", nullable = false, length = 255)
    private String nom;

    @Column(nullable = false, length = 255)
    private String nomReel;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long taille;

    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(nullable = false)
    private String loginDepot;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date dateDepot;

    @Column(nullable = false)
    private String loginSuppression;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date dateSuppression;

    @PrePersist
    public void prePersist() {
        if (dateSuppression == null) {
            dateSuppression = new Date();
        }
        if (loginSuppression == null) {
            loginSuppression = ServiceContext.getUtilisateur() != null ? ServiceContext.getUtilisateur().getLogin() : "(auto)";
        }
    }
}