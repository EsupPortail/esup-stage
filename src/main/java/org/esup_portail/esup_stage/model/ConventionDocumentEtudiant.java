package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ConventionDocumentEtudiant")
@Data
public class ConventionDocumentEtudiant extends ObjetMetier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idConventionDocumentEtudiant", nullable = false)
    private int id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idConvention", nullable = false)
    private Convention convention;

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
}