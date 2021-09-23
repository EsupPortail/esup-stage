package fr.dauphine.estage.model;

import fr.dauphine.estage.enums.RoleEnum;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "Role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRole", nullable = false)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RoleEnum code;

    @Column(name = "roleLibelle", unique = true)
    private String libelle;

    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
    private List<RoleAppFonction> roleAppFonctions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public RoleEnum getCode() {
        return code;
    }

    public void setCode(RoleEnum code) {
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
    }
}
