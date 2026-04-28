package org.esup_portail.esup_stage.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Effectif;
import org.esup_portail.esup_stage.model.NafN5;
import org.esup_portail.esup_stage.model.Pays;
import org.esup_portail.esup_stage.model.StatutJuridique;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.model.TypeStructure;

import java.util.Date;

@Data
public class StructureDto {

    @JsonView(Views.List.class)
    private Integer id;
    private String libCedex;
    private String codeEtab;

    @JsonView(Views.List.class)
    private String numeroSiret;

    @JsonView(Views.List.class)
    private NafN5 nafN5;

    @JsonView(Views.List.class)
    private String raisonSociale;

    private String activitePrincipale;
    private String telephone;
    private String fax;
    private String mail;
    private String siteWeb;
    private String groupe;
    private String logo;
    private boolean estValidee;
    private String loginValidation;
    private Date dateValidation;
    private String loginStopValidation;
    private Date dateStopValidation;
    private Date infosAJour;
    private String loginInfosAJour;
    private Effectif effectif;

    @JsonView(Views.List.class)
    private StatutJuridique statutJuridique;

    @JsonView(Views.List.class)
    private TypeStructure typeStructure;

    private String nomDirigeant;
    private String prenomDirigeant;
    private Boolean temEnServStructure;
    private String batimentResidence;
    private String voie;

    @JsonView(Views.List.class)
    private String commune;

    private String codePostal;
    private String codeCommune;

    @JsonView(Views.List.class)
    private Pays pays;

    @JsonView(Views.List.class)
    private String numeroRNE;
    private boolean temSiren;
    private boolean verrouillageSynchroStructureSirene;

    @JsonView(Views.List.class)
    private boolean confidentialiteCoordonnees;
    private CentreGestion centreGestionProprietaire;
    private String loginCreation;
    private Date dateCreation;
    private String loginModif;
    private Date dateModif;

    public static StructureDto from(Structure structure, boolean hideCoordinates) {
        StructureDto dto = new StructureDto();
        dto.setId(structure.getId());
        dto.setCodeEtab(structure.getCodeEtab());
        dto.setNumeroSiret(structure.getNumeroSiret());
        dto.setNafN5(structure.getNafN5());
        dto.setRaisonSociale(structure.getRaisonSociale());
        dto.setActivitePrincipale(structure.getActivitePrincipale());
        dto.setSiteWeb(structure.getSiteWeb());
        dto.setGroupe(structure.getGroupe());
        dto.setLogo(structure.getLogo());
        dto.setEstValidee(structure.isEstValidee());
        dto.setLoginValidation(structure.getLoginValidation());
        dto.setDateValidation(structure.getDateValidation());
        dto.setLoginStopValidation(structure.getLoginStopValidation());
        dto.setDateStopValidation(structure.getDateStopValidation());
        dto.setInfosAJour(structure.getInfosAJour());
        dto.setLoginInfosAJour(structure.getLoginInfosAJour());
        dto.setEffectif(structure.getEffectif());
        dto.setStatutJuridique(structure.getStatutJuridique());
        dto.setTypeStructure(structure.getTypeStructure());
        dto.setNomDirigeant(structure.getNomDirigeant());
        dto.setPrenomDirigeant(structure.getPrenomDirigeant());
        dto.setTemEnServStructure(structure.getTemEnServStructure());
        dto.setPays(structure.getPays());
        dto.setNumeroRNE(structure.getNumeroRNE());
        dto.setTemSiren(structure.isTemSiren());
        dto.setVerrouillageSynchroStructureSirene(structure.isVerrouillageSynchroStructureSirene());
        dto.setConfidentialiteCoordonnees(structure.isConfidentialiteCoordonnees());
        dto.setCentreGestionProprietaire(structure.getCentreGestionProprietaire());
        dto.setLoginCreation(structure.getLoginCreation());
        dto.setDateCreation(structure.getDateCreation());
        dto.setLoginModif(structure.getLoginModif());
        dto.setDateModif(structure.getDateModif());

        if (!hideCoordinates) {
            dto.setTelephone(structure.getTelephone());
            dto.setFax(structure.getFax());
            dto.setMail(structure.getMail());
            dto.setBatimentResidence(structure.getBatimentResidence());
            dto.setVoie(structure.getVoie());
            dto.setCommune(structure.getCommune());
            dto.setCodePostal(structure.getCodePostal());
            dto.setCodeCommune(structure.getCodeCommune());
            dto.setLibCedex(structure.getLibCedex());
        }

        return dto;
    }
}
