package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;

@Entity
@Table(name = "AppFonction")
@Data
public class AppFonction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAppFonction", nullable = false)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "codeAppFonction", nullable = false, unique = true)
    private AppFonctionEnum code;

    @Column(name = "libelleAppFonction", nullable = false, unique = true)
    private String libelle;

}
