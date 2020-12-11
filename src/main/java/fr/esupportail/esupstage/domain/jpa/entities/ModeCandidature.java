package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the ModeCandidature database table.
 *
 */
@Entity
@Table(name = "ModeCandidature")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "ModeCandidature.findAll", query = "SELECT m FROM ModeCandidature m")
public class ModeCandidature implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idModeCandidature;
    @Column(nullable = false, length = 20)
    private String codeCtrl;
    @Column(nullable = false, length = 50)
    private String libelleModeCandidature;
    @Column(nullable = false, length = 1)
    private String temEnServModeCandidature;
    // bi-directional many-to-many association to Offre
    @ManyToMany(mappedBy = "modeCandidatures")
    private List<Offre> offres;

}
