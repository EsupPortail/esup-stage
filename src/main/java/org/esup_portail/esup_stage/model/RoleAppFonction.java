package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AppFonction getAppFonction() {
        return appFonction;
    }

    public void setAppFonction(AppFonction appFonction) {
        this.appFonction = appFonction;
    }

    public boolean isLecture() {
        return lecture;
    }

    public void setLecture(boolean lecture) {
        this.lecture = lecture;
    }

    public boolean isCreation() {
        return creation;
    }

    public void setCreation(boolean creation) {
        this.creation = creation;
    }

    public boolean isModification() {
        return modification;
    }

    public void setModification(boolean modification) {
        this.modification = modification;
    }

    public boolean isSuppression() {
        return suppression;
    }

    public void setSuppression(boolean suppression) {
        this.suppression = suppression;
    }

    public boolean isValidation() {
        return validation;
    }

    public void setValidation(boolean validation) {
        this.validation = validation;
    }
}
