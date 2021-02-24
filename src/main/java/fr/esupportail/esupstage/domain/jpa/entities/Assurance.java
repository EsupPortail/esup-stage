package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
    private Integer idAssurance;
    @Column(nullable = false, length = 20)
    private String codeCtrl;
    @Column(nullable = false, length = 100)
    private String libelleAssurance;
    @Column(nullable = false, length = 1)
    private String temEnServAss;
}