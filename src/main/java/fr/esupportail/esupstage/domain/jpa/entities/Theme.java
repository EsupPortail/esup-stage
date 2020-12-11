package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the Theme database table.
 *
 */
@Entity
@Table(name = "Theme")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Theme.findAll", query = "SELECT t FROM Theme t")
public class Theme implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idTheme;
    @Column(nullable = false, length = 50)
    private String libelleTheme;
    @Column(length = 1)
    private String temEnServTheme;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "theme")
    private List<Convention> conventions;

    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setTheme(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setTheme(null);
        return convention;
    }
}