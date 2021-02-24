package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
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
