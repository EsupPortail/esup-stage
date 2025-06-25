package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "RoleAppFonction", uniqueConstraints = {@UniqueConstraint(name = "uniq_RoleAppFonction_Role_AppFonction", columnNames = {"idRole", "idAppFonction"})})
public class RoleAppFonction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRoleAppFonction", nullable = false)
    private int id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idRole", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idAppFonction", nullable = false)
    private AppFonction appFonction;

    @Column(nullable = false)
    private boolean lecture;

    @Column(nullable = false)
    private boolean creation;

    @Column(nullable = false)
    private boolean modification;

    @Column(nullable = false)
    private boolean suppression;

    @Column(nullable = false)
    private boolean validation;

}
