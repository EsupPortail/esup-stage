package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.enums.OperationType;

import java.time.LocalDateTime;

@Entity
@Data
public class HistoriqueStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "structureId")
    private Structure structure;

    @ManyToOne
    @JoinColumn(name = "utilisateurId")
    private Utilisateur utilisateur;

    @Column
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Column
    private LocalDateTime operationDate;

    @Column
    private String etatPrecedent;

    @Column
    private String etatActuel;

}
