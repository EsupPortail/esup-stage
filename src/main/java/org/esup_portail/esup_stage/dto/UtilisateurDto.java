package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.AppFonction;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.RoleAppFonction;
import org.esup_portail.esup_stage.model.Utilisateur;

import java.util.ArrayList;
import java.util.List;

@Data
public class UtilisateurDto {

    private int id;
    private String login;
    private String uid;
    private String nom;
    private String prenom;
    private Boolean actif;
    private String numEtudiant;
    private List<RoleDto> roles = new ArrayList<>();

    public static UtilisateurDto from(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }
        UtilisateurDto dto = new UtilisateurDto();
        dto.setId(utilisateur.getId());
        dto.setLogin(utilisateur.getLogin());
        dto.setUid(utilisateur.getUid());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setActif(utilisateur.getActif());
        dto.setNumEtudiant(utilisateur.getNumEtudiant());
        if (utilisateur.getRoles() != null) {
            dto.setRoles(utilisateur.getRoles().stream().map(RoleDto::from).toList());
        }
        return dto;
    }

    @Data
    public static class RoleDto {
        private int id;
        private String code;
        private String libelle;
        private String origine;
        private List<RoleAppFonctionDto> roleAppFonctions = new ArrayList<>();

        public static RoleDto from(Role role) {
            if (role == null) {
                return null;
            }
            RoleDto dto = new RoleDto();
            dto.setId(role.getId());
            dto.setCode(role.getCode());
            dto.setLibelle(role.getLibelle());
            dto.setOrigine(role.getOrigine());
            if (role.getRoleAppFonctions() != null) {
                dto.setRoleAppFonctions(role.getRoleAppFonctions().stream().map(RoleAppFonctionDto::from).toList());
            }
            return dto;
        }
    }

    @Data
    public static class RoleAppFonctionDto {
        private int id;
        private AppFonctionDto appFonction;
        private boolean lecture;
        private boolean creation;
        private boolean modification;
        private boolean suppression;
        private boolean validation;

        public static RoleAppFonctionDto from(RoleAppFonction roleAppFonction) {
            if (roleAppFonction == null) {
                return null;
            }
            RoleAppFonctionDto dto = new RoleAppFonctionDto();
            dto.setId(roleAppFonction.getId());
            dto.setAppFonction(AppFonctionDto.from(roleAppFonction.getAppFonction()));
            dto.setLecture(roleAppFonction.isLecture());
            dto.setCreation(roleAppFonction.isCreation());
            dto.setModification(roleAppFonction.isModification());
            dto.setSuppression(roleAppFonction.isSuppression());
            dto.setValidation(roleAppFonction.isValidation());
            return dto;
        }
    }

    @Data
    public static class AppFonctionDto {
        private int id;
        private String code;
        private String libelle;

        public static AppFonctionDto from(AppFonction appFonction) {
            if (appFonction == null) {
                return null;
            }
            AppFonctionDto dto = new AppFonctionDto();
            dto.setId(appFonction.getId());
            dto.setCode(appFonction.getCode().name());
            dto.setLibelle(appFonction.getLibelle());
            return dto;
        }
    }
}
