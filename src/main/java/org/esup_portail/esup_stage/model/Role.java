package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.esup_portail.esup_stage.dto.view.Views;

import java.util.List;

@Data
@Entity
@Table(name = "Role")
@EqualsAndHashCode(of = {"code"})
public class Role implements Exportable {

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
    @Column(nullable = false, unique = true)
    private String code;

    @JsonView(Views.List.class)
    @Column(name = "roleLibelle", unique = true)
    private String libelle;

    @JsonView(Views.List.class)
    @Column(name = "roleOrigine")
    private String origine;


    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<RoleAppFonction> roleAppFonctions;

    public void setRoleAppFonctions(List<RoleAppFonction> roleAppFonctions) {
        this.roleAppFonctions = roleAppFonctions;
        for (RoleAppFonction roleAppFonction : this.roleAppFonctions) {
            if (roleAppFonction.getRole() == null) {
                roleAppFonction.setRole(this);
            }
        }
    }

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "code":
                value = getCode();
                break;
            case "libelle":
                value = getLibelle();
                break;
            default:
                break;
        }
        return value;
    }
}
