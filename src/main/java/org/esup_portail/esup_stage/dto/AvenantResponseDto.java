package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.Civilite;
import org.esup_portail.esup_stage.model.Contact;
import org.esup_portail.esup_stage.model.Devise;
import org.esup_portail.esup_stage.model.Enseignant;
import org.esup_portail.esup_stage.model.ModeVersGratification;
import org.esup_portail.esup_stage.model.Pays;
import org.esup_portail.esup_stage.model.Service;
import org.esup_portail.esup_stage.model.UniteDuree;
import org.esup_portail.esup_stage.model.UniteGratification;

import java.util.Date;

@Data
public class AvenantResponseDto {

    private int id;
    private int idConvention;
    private String titreAvenant;
    private String motifAvenant;
    private boolean rupture;
    private boolean modificationPeriode;
    private Date dateDebutStage;
    private Date dateFinStage;
    private boolean interruptionStage;
    private Date dateDebutInterruption;
    private Date dateFinInterruption;
    private Boolean modificationLieu;
    private ServiceDto service;
    private boolean modificationSujet;
    private String sujetStage;
    private boolean modificationEnseignant;
    private boolean modificationSalarie;
    private ContactDto contact;
    private boolean validationAvenant;
    private EnseignantDto enseignant;
    private String montantGratification;
    private ReferentielDto uniteGratification;
    private ReferentielDto modeVersGratification;
    private ReferentielDto devise;
    private boolean modificationMontantGratification;
    private Date dateRupture;
    private String commentaireRupture;
    private String monnaieGratification;
    private ReferentielDto uniteDuree;
    private Date dateValidation;
    private Date dateEnvoiSignature;
    private String documentId;
    private Date dateSignatureEtudiant;
    private Date dateDepotEtudiant;
    private Date dateSignatureEnseignant;
    private Date dateDepotEnseignant;
    private Date dateSignatureTuteur;
    private Date dateDepotTuteur;
    private Date dateSignatureSignataire;
    private Date dateDepotSignataire;
    private Date dateSignatureViseur;
    private Date dateDepotViseur;
    private Date dateActualisationSignature;
    private String loginEnvoiSignature;
    private boolean temAvenantSigne;
    private boolean allSignedDateSetted;

    public static AvenantResponseDto from(Avenant avenant) {
        if (avenant == null) {
            return null;
        }
        AvenantResponseDto dto = new AvenantResponseDto();
        dto.setId(avenant.getId());
        dto.setIdConvention(avenant.getConvention() != null ? avenant.getConvention().getId() : 0);
        dto.setTitreAvenant(avenant.getTitreAvenant());
        dto.setMotifAvenant(avenant.getMotifAvenant());
        dto.setRupture(avenant.isRupture());
        dto.setModificationPeriode(avenant.isModificationPeriode());
        dto.setDateDebutStage(avenant.getDateDebutStage());
        dto.setDateFinStage(avenant.getDateFinStage());
        dto.setInterruptionStage(avenant.isInterruptionStage());
        dto.setDateDebutInterruption(avenant.getDateDebutInterruption());
        dto.setDateFinInterruption(avenant.getDateFinInterruption());
        dto.setModificationLieu(avenant.getModificationLieu());
        dto.setService(ServiceDto.from(avenant.getService()));
        dto.setModificationSujet(avenant.isModificationSujet());
        dto.setSujetStage(avenant.getSujetStage());
        dto.setModificationEnseignant(avenant.isModificationEnseignant());
        dto.setModificationSalarie(avenant.isModificationSalarie());
        dto.setContact(ContactDto.from(avenant.getContact()));
        dto.setValidationAvenant(avenant.isValidationAvenant());
        dto.setEnseignant(EnseignantDto.from(avenant.getEnseignant()));
        dto.setMontantGratification(avenant.getMontantGratification());
        dto.setUniteGratification(ReferentielDto.from(avenant.getUniteGratification()));
        dto.setModeVersGratification(ReferentielDto.from(avenant.getModeVersGratification()));
        dto.setDevise(ReferentielDto.from(avenant.getDevise()));
        dto.setModificationMontantGratification(avenant.isModificationMontantGratification());
        dto.setDateRupture(avenant.getDateRupture());
        dto.setCommentaireRupture(avenant.getCommentaireRupture());
        dto.setMonnaieGratification(avenant.getMonnaieGratification());
        dto.setUniteDuree(ReferentielDto.from(avenant.getUniteDuree()));
        dto.setDateValidation(avenant.getDateValidation());
        dto.setDateEnvoiSignature(avenant.getDateEnvoiSignature());
        dto.setDocumentId(avenant.getDocumentId());
        dto.setDateSignatureEtudiant(avenant.getDateSignatureEtudiant());
        dto.setDateDepotEtudiant(avenant.getDateDepotEtudiant());
        dto.setDateSignatureEnseignant(avenant.getDateSignatureEnseignant());
        dto.setDateDepotEnseignant(avenant.getDateDepotEnseignant());
        dto.setDateSignatureTuteur(avenant.getDateSignatureTuteur());
        dto.setDateDepotTuteur(avenant.getDateDepotTuteur());
        dto.setDateSignatureSignataire(avenant.getDateSignatureSignataire());
        dto.setDateDepotSignataire(avenant.getDateDepotSignataire());
        dto.setDateSignatureViseur(avenant.getDateSignatureViseur());
        dto.setDateDepotViseur(avenant.getDateDepotViseur());
        dto.setDateActualisationSignature(avenant.getDateActualisationSignature());
        dto.setLoginEnvoiSignature(avenant.getLoginEnvoiSignature());
        dto.setTemAvenantSigne(avenant.isTemAvenantSigne());
        dto.setAllSignedDateSetted(avenant.isAllSignedDateSetted());
        return dto;
    }

    @Data
    public static class ReferentielDto {
        private int id;
        private String libelle;

        public static ReferentielDto from(UniteGratification uniteGratification) {
            if (uniteGratification == null) {
                return null;
            }
            ReferentielDto dto = new ReferentielDto();
            dto.setId(uniteGratification.getId());
            dto.setLibelle(uniteGratification.getLibelle());
            return dto;
        }

        public static ReferentielDto from(UniteDuree uniteDuree) {
            if (uniteDuree == null) {
                return null;
            }
            ReferentielDto dto = new ReferentielDto();
            dto.setId(uniteDuree.getId());
            dto.setLibelle(uniteDuree.getLibelle());
            return dto;
        }

        public static ReferentielDto from(ModeVersGratification modeVersGratification) {
            if (modeVersGratification == null) {
                return null;
            }
            ReferentielDto dto = new ReferentielDto();
            dto.setId(modeVersGratification.getId());
            dto.setLibelle(modeVersGratification.getLibelle());
            return dto;
        }

        public static ReferentielDto from(Devise devise) {
            if (devise == null) {
                return null;
            }
            ReferentielDto dto = new ReferentielDto();
            dto.setId(devise.getId());
            dto.setLibelle(devise.getLibelle());
            return dto;
        }
    }

    @Data
    public static class ServiceDto {
        private int id;
        private String nom;
        private String voie;
        private String codePostal;
        private String commune;
        private PaysDto pays;
        private String telephone;

        public static ServiceDto from(Service service) {
            if (service == null) {
                return null;
            }
            ServiceDto dto = new ServiceDto();
            dto.setId(service.getId());
            dto.setNom(service.getNom());
            dto.setVoie(service.getVoie());
            dto.setCodePostal(service.getCodePostal());
            dto.setCommune(service.getCommune());
            dto.setPays(PaysDto.from(service.getPays()));
            dto.setTelephone(service.getTelephone());
            return dto;
        }
    }

    @Data
    public static class PaysDto {
        private int id;
        private String lib;

        public static PaysDto from(Pays pays) {
            if (pays == null) {
                return null;
            }
            PaysDto dto = new PaysDto();
            dto.setId(pays.getId());
            dto.setLib(pays.getLib());
            return dto;
        }
    }

    @Data
    public static class ContactDto {
        private int id;
        private String nom;
        private String prenom;
        private CiviliteDto civilite;
        private String fonction;
        private String tel;
        private String mail;
        private String fax;

        public static ContactDto from(Contact contact) {
            if (contact == null) {
                return null;
            }
            ContactDto dto = new ContactDto();
            dto.setId(contact.getId());
            dto.setNom(contact.getNom());
            dto.setPrenom(contact.getPrenom());
            dto.setCivilite(CiviliteDto.from(contact.getCivilite()));
            dto.setFonction(contact.getFonction());
            dto.setTel(contact.getTel());
            dto.setMail(contact.getMail());
            dto.setFax(contact.getFax());
            return dto;
        }
    }

    @Data
    public static class CiviliteDto {
        private int id;
        private String libelle;

        public static CiviliteDto from(Civilite civilite) {
            if (civilite == null) {
                return null;
            }
            CiviliteDto dto = new CiviliteDto();
            dto.setId(civilite.getId());
            dto.setLibelle(civilite.getLibelle());
            return dto;
        }
    }

    @Data
    public static class EnseignantDto {
        private int id;
        private String nom;
        private String prenom;
        private String typePersonne;
        private String tel;
        private String mail;

        public static EnseignantDto from(Enseignant enseignant) {
            if (enseignant == null) {
                return null;
            }
            EnseignantDto dto = new EnseignantDto();
            dto.setId(enseignant.getId());
            dto.setNom(enseignant.getNom());
            dto.setPrenom(enseignant.getPrenom());
            dto.setTypePersonne(enseignant.getTypePersonne());
            dto.setTel(enseignant.getTel());
            dto.setMail(enseignant.getMail());
            return dto;
        }
    }
}
