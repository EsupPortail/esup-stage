package org.esup_portail.esup_stage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.esup_portail.esup_stage.model.Contact;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactDto {
    private int id;
    private String nom;
    private String prenom;
    private int idCentreGestion;

    public ContactDto(Contact contact) {
        this.id = contact.getId();
        this.nom = contact.getNom();
        this.prenom = contact.getPrenom();
        this.idCentreGestion = contact.getCentreGestion().getId();
    }
}
