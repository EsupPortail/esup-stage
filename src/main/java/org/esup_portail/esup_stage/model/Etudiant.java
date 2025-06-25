package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.dto.view.Views;

import java.util.Date;

@Entity
@Table(
        name = "Etudiant",
        uniqueConstraints = {
                @UniqueConstraint(name = "index_ident_etudiant_code_universite", columnNames = {"identEtudiant", "codeUniversite"})
        }
)
@Data
public class Etudiant extends ObjetMetier implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEtudiant", nullable = false)
    private int id;

    @JsonView(Views.List.class)
    @Column(nullable = false)
    private String nom;

    @JsonView(Views.List.class)
    @Column(nullable = false)
    private String prenom;

    @Column
    private String mail;

    @Column(nullable = false)
    private String codeUniversite;

    @Column(nullable = false)
    private String identEtudiant;

    @Column
    private String nomMarital;

    @Column(nullable = false)
    private String numEtudiant;

    @Column
    private String codeSexe;

    @Temporal(TemporalType.DATE)
    private Date dateNais;

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
