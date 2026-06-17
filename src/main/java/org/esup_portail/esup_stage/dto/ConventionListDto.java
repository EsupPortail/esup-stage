package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.Enseignant;
import org.esup_portail.esup_stage.model.Etape;
import org.esup_portail.esup_stage.model.Etudiant;
import org.esup_portail.esup_stage.model.FicheEvaluation;
import org.esup_portail.esup_stage.model.ReponseEvaluation;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.model.Ufr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class ConventionListDto {

    private int id;
    private PersonneDto etudiant;
    private CentreGestionDto centreGestion;
    private ReferentielDto ufr;
    private ReferentielDto etape;
    private PersonneDto enseignant;
    private StructureDto structure;
    private String sujetStage;
    private Date dateDebutStage;
    private Date dateFinStage;
    private Boolean validationPedagogique;
    private Boolean validationConvention;
    private Boolean verificationAdministrative;
    private String annee;
    private Boolean envoiMailEtudiant;
    private Date dateEnvoiMailEtudiant;
    private Boolean envoiMailTuteurPedago;
    private Date dateEnvoiMailTuteurPedago;
    private Boolean envoiMailTuteurPro;
    private Date dateEnvoiMailTuteurPro;
    private List<IdDto> avenants = new ArrayList<>();
    private ReponseEvaluationListDto reponseEvaluation;
    private Date dateEnvoiSignature;
    private Date dateSignatureEtudiant;
    private Date dateSignatureEnseignant;
    private Date dateSignatureTuteur;
    private Date dateSignatureSignataire;
    private Date dateSignatureViseur;
    private String lieuStage;

    public static ConventionListDto from(Convention convention) {
        ConventionListDto dto = new ConventionListDto();
        dto.setId(convention.getId());
        dto.setEtudiant(PersonneDto.from(convention.getEtudiant()));
        dto.setCentreGestion(CentreGestionDto.from(convention.getCentreGestion()));
        dto.setUfr(ReferentielDto.from(convention.getUfr()));
        dto.setEtape(ReferentielDto.from(convention.getEtape()));
        dto.setEnseignant(PersonneDto.from(convention.getEnseignant()));
        dto.setStructure(StructureDto.from(convention.getStructure()));
        dto.setSujetStage(convention.getSujetStage());
        dto.setDateDebutStage(convention.getDateDebutStage());
        dto.setDateFinStage(convention.getDateFinStage());
        dto.setValidationPedagogique(convention.getValidationPedagogique());
        dto.setValidationConvention(convention.getValidationConvention());
        dto.setVerificationAdministrative(convention.getVerificationAdministrative());
        dto.setAnnee(convention.getAnnee());
        dto.setEnvoiMailEtudiant(convention.getEnvoiMailEtudiant());
        dto.setDateEnvoiMailEtudiant(convention.getDateEnvoiMailEtudiant());
        dto.setEnvoiMailTuteurPedago(convention.getEnvoiMailTuteurPedago());
        dto.setDateEnvoiMailTuteurPedago(convention.getDateEnvoiMailTuteurPedago());
        dto.setEnvoiMailTuteurPro(convention.getEnvoiMailTuteurPro());
        dto.setDateEnvoiMailTuteurPro(convention.getDateEnvoiMailTuteurPro());
        dto.setReponseEvaluation(ReponseEvaluationListDto.from(convention.getReponseEvaluation()));
        dto.setDateEnvoiSignature(convention.getDateEnvoiSignature());
        dto.setDateSignatureEtudiant(convention.getDateSignatureEtudiant());
        dto.setDateSignatureEnseignant(convention.getDateSignatureEnseignant());
        dto.setDateSignatureTuteur(convention.getDateSignatureTuteur());
        dto.setDateSignatureSignataire(convention.getDateSignatureSignataire());
        dto.setDateSignatureViseur(convention.getDateSignatureViseur());
        dto.setLieuStage(convention.getLieuStage());
        if (convention.getAvenants() != null) {
            dto.setAvenants(convention.getAvenants().stream().map(IdDto::from).toList());
        }
        return dto;
    }

    @Data
    public static class IdDto {
        private int id;

        public static IdDto from(Avenant avenant) {
            IdDto dto = new IdDto();
            dto.setId(avenant.getId());
            return dto;
        }
    }

    @Data
    public static class ReferentielDto {
        private Object id;
        private String libelle;

        public static ReferentielDto from(Ufr ufr) {
            if (ufr == null) {
                return null;
            }
            ReferentielDto dto = new ReferentielDto();
            dto.setId(ufr.getId());
            dto.setLibelle(ufr.getLibelle());
            return dto;
        }

        public static ReferentielDto from(Etape etape) {
            if (etape == null) {
                return null;
            }
            ReferentielDto dto = new ReferentielDto();
            dto.setId(etape.getId());
            dto.setLibelle(etape.getLibelle());
            return dto;
        }
    }

    @Data
    public static class PersonneDto {
        private int id;
        private String nom;
        private String prenom;
        private String identEtudiant;
        private String numEtudiant;
        private String uidEnseignant;

        public static PersonneDto from(Etudiant etudiant) {
            if (etudiant == null) {
                return null;
            }
            PersonneDto dto = new PersonneDto();
            dto.setId(etudiant.getId());
            dto.setNom(etudiant.getNom());
            dto.setPrenom(etudiant.getPrenom());
            dto.setIdentEtudiant(etudiant.getIdentEtudiant());
            dto.setNumEtudiant(etudiant.getNumEtudiant());
            return dto;
        }

        public static PersonneDto from(Enseignant enseignant) {
            if (enseignant == null) {
                return null;
            }
            PersonneDto dto = new PersonneDto();
            dto.setId(enseignant.getId());
            dto.setNom(enseignant.getNom());
            dto.setPrenom(enseignant.getPrenom());
            dto.setUidEnseignant(enseignant.getUidEnseignant());
            return dto;
        }
    }

    @Data
    public static class StructureDto {
        private int id;
        private String raisonSociale;

        public static StructureDto from(Structure structure) {
            if (structure == null) {
                return null;
            }
            StructureDto dto = new StructureDto();
            dto.setId(structure.getId());
            dto.setRaisonSociale(structure.getRaisonSociale());
            return dto;
        }
    }

    @Data
    public static class CentreGestionDto {
        private int id;
        private String nomCentre;
        private boolean validationPedagogique;
        private boolean verificationAdministrative;
        private boolean validationConvention;
        private FicheEvaluationListDto ficheEvaluation;

        public static CentreGestionDto from(CentreGestion centreGestion) {
            if (centreGestion == null) {
                return null;
            }
            CentreGestionDto dto = new CentreGestionDto();
            dto.setId(centreGestion.getId());
            dto.setNomCentre(centreGestion.getNomCentre());
            dto.setValidationPedagogique(Boolean.TRUE.equals(centreGestion.getValidationPedagogique()));
            dto.setVerificationAdministrative(Boolean.TRUE.equals(centreGestion.getVerificationAdministrative()));
            dto.setValidationConvention(Boolean.TRUE.equals(centreGestion.getValidationConvention()));
            dto.setFicheEvaluation(FicheEvaluationListDto.from(centreGestion.getFicheEvaluation()));
            return dto;
        }
    }

    @Data
    public static class FicheEvaluationListDto {
        private Boolean validationEtudiant;
        private Boolean validationEnseignant;
        private Boolean validationEntreprise;

        public static FicheEvaluationListDto from(FicheEvaluation ficheEvaluation) {
            if (ficheEvaluation == null) {
                return null;
            }
            FicheEvaluationListDto dto = new FicheEvaluationListDto();
            dto.setValidationEtudiant(ficheEvaluation.getValidationEtudiant());
            dto.setValidationEnseignant(ficheEvaluation.getValidationEnseignant());
            dto.setValidationEntreprise(ficheEvaluation.getValidationEntreprise());
            return dto;
        }
    }

    @Data
    public static class ReponseEvaluationListDto {
        private Boolean validationEtudiant;
        private Boolean validationEnseignant;
        private Boolean validationEntreprise;
        private Boolean impressionEtudiant;
        private Boolean impressionEnseignant;
        private Boolean impressionEntreprise;

        public static ReponseEvaluationListDto from(ReponseEvaluation reponseEvaluation) {
            if (reponseEvaluation == null) {
                return null;
            }
            ReponseEvaluationListDto dto = new ReponseEvaluationListDto();
            dto.setValidationEtudiant(reponseEvaluation.getValidationEtudiant());
            dto.setValidationEnseignant(reponseEvaluation.getValidationEnseignant());
            dto.setValidationEntreprise(reponseEvaluation.getValidationEntreprise());
            dto.setImpressionEtudiant(reponseEvaluation.getImpressionEtudiant());
            dto.setImpressionEnseignant(reponseEvaluation.getImpressionEnseignant());
            dto.setImpressionEntreprise(reponseEvaluation.getImpressionEntreprise());
            return dto;
        }
    }
}
