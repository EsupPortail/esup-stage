package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Civilite;

import java.util.Date;

@Data
public class ContactDetailDto {
    private int id;
    private String nom;
    private String prenom;
    private String mail;
    private String tel;
    private String telephone;
    private String fonction;
    private Civilite civilite;
    private String fax;
    private int idCentreGestion;
    private CentreGestionDto centreGestionnaire;
    private Boolean refusEtreContacte;
    private Date dateRefusEtreContacte;
    private Date dateEnvoiMailOpposition;

    @Data
    public static class CentreGestionDto {
        private int id;
        private String nomCentre;

        public static CentreGestionDto from(CentreGestion centreGestion) {
            if (centreGestion == null) {
                return null;
            }
            CentreGestionDto dto = new CentreGestionDto();
            dto.setId(centreGestion.getId());
            dto.setNomCentre(centreGestion.getNomCentre());
            return dto;
        }
    }
}
