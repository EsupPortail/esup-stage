package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the ModeVersGratification database table.
 *
 */
@Entity
@Table(name = "ModeVersGratification")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "ModeVersGratification.findAll", query = "SELECT m FROM ModeVersGratification m")
public class ModeVersGratification implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idModeVersGratification;
    @Column(nullable = false, length = 50)
    private String libelleModeVersGratification;
    @Column(nullable = false, length = 1)
    private String temEnServModeVersGrat;
}