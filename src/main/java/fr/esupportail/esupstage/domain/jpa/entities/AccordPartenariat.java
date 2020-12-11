package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;

/**
 * The persistent class for the AccordPartenariat database table.
 *
 */
@Entity
@Table(name = "AccordPartenariat")
@NamedQuery(name = "AccordPartenariat.findAll", query = "SELECT a FROM AccordPartenariat a")
@Getter
@Setter
@NoArgsConstructor
public class AccordPartenariat implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idAccordPartenariat;
    @Column(nullable = false)
    private boolean comptesSupprimes;
    @Column(nullable = false)
    private Date dateCreation;
    private Date dateSuppressionComptes;
    private Date dateValidation;
    @Column(nullable = false)
    private boolean estValide;
    @Column(nullable = false, length = 50)
    private String loginCreation;
    @Column(length = 50)
    private String loginSuppressionComptes;
    @Column(length = 50)
    private String loginValidation;
    @Lob
    private String raisonSuppressionComptes;
    // bi-directional many-to-one association to Contact
    @ManyToOne
    @JoinColumn(name = "idContact", nullable = false)
    private Contact contact;
    // bi-directional many-to-one association to Structure
    @ManyToOne
    @JoinColumn(name = "idStructure", nullable = false)
    private Structure structure;


}