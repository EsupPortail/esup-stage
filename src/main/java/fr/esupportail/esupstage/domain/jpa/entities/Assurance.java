package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the Assurance database table.
 *
 */
@Entity
@Table(name = "Assurance")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Assurance.findAll", query = "SELECT a FROM Assurance a")
public class Assurance implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idAssurance;
    @Column(nullable = false, length = 20)
    private String codeCtrl;
    @Column(nullable = false, length = 100)
    private String libelleAssurance;
    @Column(nullable = false, length = 1)
    private String temEnServAss;
}