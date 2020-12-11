package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the Niveau database table.
 *
 */
@Entity
@Table(name = "Niveau")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Niveau.findAll", query = "SELECT n FROM Niveau n")
public class Niveau implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idNiveau;
    @Column(nullable = false)
    private Integer valeur;
    // bi-directional many-to-one association to Critere
    @OneToMany(mappedBy = "niveauBean")
    private List<Critere> criteres;

    public Critere addCritere(Critere critere) {
        getCriteres().add(critere);
        critere.setNiveauBean(this);
        return critere;
    }

    public Critere removeCritere(Critere critere) {
        getCriteres().remove(critere);
        critere.setNiveauBean(null);
        return critere;
    }
}