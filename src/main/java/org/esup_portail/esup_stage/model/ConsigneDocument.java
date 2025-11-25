package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ConsigneDocument")
@Data
public class ConsigneDocument extends ObjetMetier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idConsigneDocument", nullable = false)
    private int id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idConsigne", nullable = false)
    private Consigne consigne;

    @Column(name = "nomDocument", nullable = false)
    private String nom;

    @Column(nullable = false)
    private String nomReel;
}
