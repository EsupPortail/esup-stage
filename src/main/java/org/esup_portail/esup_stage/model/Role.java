package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "Role")
public class Role {

    public static final String ADM = "ADM";
    public static final String RESP_GES = "RESP_GES";
    public static final String GES = "GES";
    public static final String ENS = "ENS";
    public static final String ETU = "ETU";

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRole", nullable = false)
    private int id;

    @JsonView(Views.List.class)
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @JsonView(Views.List.class)
    @Column(name = "roleLibelle", unique = true)
    private String libelle;

    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER, cascade = {CascadeType.MERGE}, orphanRemoval = true)
    private List<RoleAppFonction> roleAppFonctions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public List<RoleAppFonction> getRoleAppFonctions() {
        return roleAppFonctions;
    }

    public void setRoleAppFonctions(List<RoleAppFonction> roleAppFonctions) {
        this.roleAppFonctions = roleAppFonctions;
        for (RoleAppFonction roleAppFonction : this.roleAppFonctions) {
            if (roleAppFonction.getRole() == null) {
                roleAppFonction.setRole(this);
            }
        }
    }
}
