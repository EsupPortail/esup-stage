package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
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