package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.ConventionDocumentEtudiant;

import java.util.Date;

@Data
public class ConventionDocumentEtudiantDto {
    private int id;
    private String nomReel;
    private String contentType;
    private Long taille;
    private String sha256;
    private String loginCreation;
    private Date dateCreation;

    public static ConventionDocumentEtudiantDto from(ConventionDocumentEtudiant document) {
        ConventionDocumentEtudiantDto dto = new ConventionDocumentEtudiantDto();
        dto.setId(document.getId());
        dto.setNomReel(document.getNomReel());
        dto.setContentType(document.getContentType());
        dto.setTaille(document.getTaille());
        dto.setSha256(document.getSha256());
        dto.setLoginCreation(document.getLoginCreation());
        dto.setDateCreation(document.getDateCreation());
        return dto;
    }
}