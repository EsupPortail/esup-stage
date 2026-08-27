package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
        name = "UtilisateurCentreGestionRole",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uniq_UtilisateurCentreGestionRole",
                        columnNames = {"idUtilisateur", "idCentreGestion", "idRole"}
                )
        }
)
public class UtilisateurCentreGestionRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUtilisateurCentreGestionRole", nullable = false)
    private int id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idUtilisateur", nullable = false)
    private Utilisateur utilisateur;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idRole", nullable = false)
    private Role role;
}
