package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Confidentialite;
import org.esup_portail.esup_stage.model.NiveauCentre;
import org.esup_portail.esup_stage.model.PersonnelCentreGestion;

import java.util.ArrayList;
import java.util.List;

@Data
public class CentreGestionListDto {

    private int id;
    private String nomCentre;
    private NiveauCentreDto niveauCentre;
    private Boolean validationPedagogique;
    private ConfidentialiteDto codeConfidentialite;
    private List<PersonnelDto> personnels = new ArrayList<>();

    public static CentreGestionListDto from(CentreGestion centreGestion) {
        CentreGestionListDto dto = new CentreGestionListDto();
        dto.setId(centreGestion.getId());
        dto.setNomCentre(centreGestion.getNomCentre());
        dto.setNiveauCentre(NiveauCentreDto.from(centreGestion.getNiveauCentre()));
        dto.setValidationPedagogique(centreGestion.getValidationPedagogique());
        dto.setCodeConfidentialite(ConfidentialiteDto.from(centreGestion.getCodeConfidentialite()));
        if (centreGestion.getPersonnels() != null) {
            dto.setPersonnels(centreGestion.getPersonnels().stream().map(PersonnelDto::from).toList());
        }
        return dto;
    }

    @Data
    public static class NiveauCentreDto {
        private int id;
        private String libelle;

        public static NiveauCentreDto from(NiveauCentre niveauCentre) {
            if (niveauCentre == null) {
                return null;
            }
            NiveauCentreDto dto = new NiveauCentreDto();
            dto.setId(niveauCentre.getId());
            dto.setLibelle(niveauCentre.getLibelle());
            return dto;
        }
    }

    @Data
    public static class ConfidentialiteDto {
        private String code;
        private String libelle;

        public static ConfidentialiteDto from(Confidentialite confidentialite) {
            if (confidentialite == null) {
                return null;
            }
            ConfidentialiteDto dto = new ConfidentialiteDto();
            dto.setCode(confidentialite.getCode());
            dto.setLibelle(confidentialite.getLibelle());
            return dto;
        }
    }

    @Data
    public static class PersonnelDto {
        private int id;
        private String uidPersonnel;

        public static PersonnelDto from(PersonnelCentreGestion personnel) {
            PersonnelDto dto = new PersonnelDto();
            dto.setId(personnel.getId());
            dto.setUidPersonnel(personnel.getUidPersonnel());
            return dto;
        }
    }
}
