package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.Contact;

/**
 * Vue allégée d'un contact pour le tableau de simulation du nettoyage (évite de sérialiser
 * l'entité JPA complète et ses relations).
 */
@Data
public class NettoyageContactDto {

    private int id;
    private String nom;
    private String prenom;
    private String mail;
    private String tel;
    private String fonction;
    private String service;
    private String structure;

    public static NettoyageContactDto from(Contact contact) {
        NettoyageContactDto dto = new NettoyageContactDto();
        dto.setId(contact.getId());
        dto.setNom(contact.getNom());
        dto.setPrenom(contact.getPrenom());
        dto.setMail(contact.getMail());
        dto.setTel(contact.getTel());
        dto.setFonction(contact.getFonction());
        if (contact.getService() != null) {
            dto.setService(contact.getService().getNom());
            if (contact.getService().getStructure() != null) {
                dto.setStructure(contact.getService().getStructure().getRaisonSociale());
            }
        }
        return dto;
    }
}
