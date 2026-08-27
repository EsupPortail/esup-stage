package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.Service;

/**
 * Vue allégée d'un service d'accueil pour le tableau de simulation du nettoyage.
 */
@Data
public class NettoyageServiceDto {

    private int id;
    private String nom;
    private String voie;
    private String codePostal;
    private String commune;
    private String structure;

    public static NettoyageServiceDto from(Service service) {
        NettoyageServiceDto dto = new NettoyageServiceDto();
        dto.setId(service.getId());
        dto.setNom(service.getNom());
        dto.setVoie(service.getVoie());
        dto.setCodePostal(service.getCodePostal());
        dto.setCommune(service.getCommune());
        if (service.getStructure() != null) {
            dto.setStructure(service.getStructure().getRaisonSociale());
        }
        return dto;
    }
}
