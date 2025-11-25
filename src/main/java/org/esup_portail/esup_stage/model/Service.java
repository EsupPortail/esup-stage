package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "Service")
public class Service extends ObjetMetier implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idService", nullable = false)
    private int id;

    @Column(nullable = false)
    private String nom;

    @Temporal(TemporalType.DATE)
    private Date infosAJour;

    @Column
    private String loginInfosAJour;

    @ManyToOne
    @JoinColumn(name = "idStructure", nullable = false)
    private Structure structure;

    @Column
    private String telephone;

    @Column
    private String batimentResidence;

    @Column(nullable = false)
    private String voie;

    @Column
    private String commune;

    @Column(nullable = false)
    private String codePostal;

    @Column
    private String codeCommune;

    @ManyToOne
    @JoinColumn(name = "idPays", nullable = false)
    private Pays pays;

    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
