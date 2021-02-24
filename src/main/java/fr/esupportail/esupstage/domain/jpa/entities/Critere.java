package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the Critere database table.
 *
 */
@Entity
@Table(name = "Critere")
@Setter
@Getter
@NoArgsConstructor
@NamedQuery(name = "Critere.findAll", query = "SELECT c FROM Critere c")
public class Critere implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
    private Integer idCritere;
    @Column(nullable = false, length = 15)
    private String clef;
    @Column(nullable = false, length = 100)
    private String valeur;
    // bi-directional many-to-one association to Categorie
    @ManyToOne
    @JoinColumn(name = "typeCategorie", nullable = false)
    private Categorie categorie;
    // bi-directional many-to-one association to Niveau
    @ManyToOne
    @JoinColumn(name = "niveau", nullable = false)
    private Niveau niveauBean;


}