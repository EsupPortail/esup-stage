package org.esup_portail.esup_stage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.esup_portail.esup_stage.model.Etudiant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonneDto {

    private String nom;
    private String prenom;

    public PersonneDto(Etudiant etudiant) {
        this.nom = etudiant.getNom();
        this.prenom = etudiant.getPrenom();
    }
}
