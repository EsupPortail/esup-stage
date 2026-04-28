package org.esup_portail.esup_stage.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Civilite;
import org.esup_portail.esup_stage.model.Contact;
import org.esup_portail.esup_stage.model.Service;

@Data
public class ContactDto {

    @JsonView(Views.List.class)
    private int id;

    @JsonView(Views.List.class)
    private String nom;

    @JsonView(Views.List.class)
    private String prenom;

    @JsonView(Views.List.class)
    private String mail;

    @JsonView(Views.List.class)
    private String tel;

    @JsonView(Views.List.class)
    private String telephone;

    @JsonView(Views.List.class)
    private String fax;

    @JsonView(Views.List.class)
    private Civilite civilite;

    @JsonView(Views.List.class)
    private String fonction;

    @JsonView(Views.List.class)
    private Service service;

    @JsonView(Views.List.class)
    private CentreGestion centreGestion;

    @JsonView(Views.List.class)
    private CentreGestion centreGestionnaire;

    @JsonView(Views.List.class)
    private String loginCreation;

    @JsonView(Views.List.class)
    private String loginModif;

    public static ContactDto from(Contact contact, boolean hideSensitiveFields) {
        ContactDto dto = new ContactDto();
        dto.setId(contact.getId());
        dto.setNom(contact.getNom());
        dto.setPrenom(contact.getPrenom());
        dto.setCivilite(contact.getCivilite());
        dto.setFonction(contact.getFonction());
        dto.setService(contact.getService());
        dto.setCentreGestion(contact.getCentreGestion());
        dto.setCentreGestionnaire(contact.getCentreGestion());
        dto.setLoginCreation(contact.getLoginCreation());
        dto.setLoginModif(contact.getLoginModif());

        if (!hideSensitiveFields) {
            dto.setMail(contact.getMail());
            dto.setTel(contact.getTel());
            dto.setTelephone(contact.getTel());
            dto.setFax(contact.getFax());
        }

        return dto;
    }
}
