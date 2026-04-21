package org.esup_portail.esup_stage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegimeInscriptionApogee {
    @Id
    @Column(name = "codeRegimeInscription")
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column(nullable = false, length = 1)
    private String temEnServ;

    @Column(nullable = false)
    private LocalDateTime dateDerniereMaj;
}
