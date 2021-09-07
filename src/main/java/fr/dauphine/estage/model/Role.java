package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "Role")
public class Role {

    @Id
    @GeneratedValue
    @Column(name = "idRole", nullable = false)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 10)
    private RoleEnum code;

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
}
