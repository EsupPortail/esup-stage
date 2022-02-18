package org.esup_portail.esup_stage.service;

import freemarker.template.Template;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.dto.SendMailTestDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.TemplateMailJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Service
public class MailerService {
    private static final Logger logger	= LogManager.getLogger(MailerService.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    TemplateMailJpaRepository templateMailJpaRepository;

    private void sendMail(String to, TemplateMail templateMail, MailContext mailContext) {
        sendMail(to, templateMail, mailContext, false);
    }

    /**
     * TODO pour le mail de modification d'une convention, récupérer le type de l'information modifié pour l'ajouter dans les paramètres
     * MailContext.convention.elementModifs est une string contenant "les infos étudiant, le tuteur professionnel, le signataire, le détail du stage, l'enseignant référent"
     * Mettre sous formation de list "les infos étudiant", "le tuteur professionnel" etc et concatener les valeurs avec ", "
     */

    public void sendAlerteValidation(String to, Convention convention, Utilisateur userModif, String templateMailCode) {
        TemplateMail templateMail = templateMailJpaRepository.findByCode(templateMailCode);
        if (templateMail == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Template mail " + templateMailCode + " non trouvé");
        }
        MailContext mailContext = new MailContext(applicationBootstrap, convention, null, userModif);
        sendMail(to, templateMail, mailContext, false);
    }

    public void sendTest(SendMailTestDto sendMailTestDto, Utilisateur utilisateur) {
        TemplateMail templateMail = templateMailJpaRepository.findByCode(sendMailTestDto.getTemplateMail());
        if (templateMail == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Template mail " + sendMailTestDto.getTemplateMail() + " non trouvé");
        }

        MailContext mailContext = new MailContext();
        mailContext.setModifiePar(new MailContext.ModifieParContext(utilisateur));
        sendMail(sendMailTestDto.getTo(), templateMail, mailContext, true);
    }

    private void sendMail(String to, TemplateMail templateMail, MailContext mailContext, boolean forceTo) {
        boolean disableDelivery = applicationBootstrap.getAppConfig().getMailerDisableDelivery();
        if (!disableDelivery) {
            String deliveryAddress = applicationBootstrap.getAppConfig().getMailerDeliveryAddress();
            if (!forceTo && deliveryAddress != null && !deliveryAddress.isEmpty()) {
                to = deliveryAddress;
            }

            try {
                Template templateObjet = new Template("template_mail_objet"+templateMail.getId(), templateMail.getObjet(), freeMarkerConfigurer.getConfiguration());
                StringWriter objet = new StringWriter();
                templateObjet.process(mailContext, objet);

                Template templateText = new Template("template_mail_text"+templateMail.getId(), templateMail.getTexte(), freeMarkerConfigurer.getConfiguration());
                StringWriter text = new StringWriter();
                templateText.process(mailContext, text);

                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
                helper.setTo(to);
                helper.setFrom(applicationBootstrap.getAppConfig().getMailerFrom());
                helper.setSubject(objet.toString());
                helper.setText(text.toString(), true);
                javaMailSender.send(message);
            } catch (Exception e) {
                logger.error("Une erreur est survenue lors de l'envoi d'un email", e);
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur technique");
            }
        } else {
            logger.info("Delivery disabled. Mail was: " + templateMail.getCode());
        }
    }

    public boolean isAlerteActif(PersonnelCentreGestion personnel, String templateCode) {
        switch (templateCode) {
            case TemplateMail.CODE_AVENANT_VALIDATION:
                return personnel.getValidationAvenant() != null && personnel.getValidationAvenant();
            case TemplateMail.CODE_CONVENTION_VALID_ADMINISTRATIVE:
                return personnel.getValidationAdministrativeConvention() != null && personnel.getValidationAdministrativeConvention();
            case TemplateMail.CODE_CONVENTION_VALID_PEDAGOGIQUE:
                return personnel.getValidationPedagogiqueConvention() != null && personnel.getValidationPedagogiqueConvention();
            case TemplateMail.CODE_ETU_CREA_AVENANT:
                return personnel.getCreationAvenantEtudiant() != null && personnel.getCreationAvenantEtudiant();
            case TemplateMail.CODE_ETU_CREA_CONVENTION:
                return personnel.getCreationConventionEtudiant() != null && personnel.getCreationConventionEtudiant();
            case TemplateMail.CODE_ETU_MODIF_AVENANT:
                return personnel.getModificationAvenantEtudiant() != null && personnel.getModificationAvenantEtudiant();
            case TemplateMail.CODE_ETU_MODIF_CONVENTION:
                return personnel.getModificationConventionEtudiant() != null && personnel.getModificationConventionEtudiant();
            case TemplateMail.CODE_GES_CREA_AVENANT:
                return personnel.getCreationAvenantGestionnaire() != null && personnel.getCreationAvenantGestionnaire();
            case TemplateMail.CODE_GES_CREA_CONVENTION:
                return personnel.getCreationConventionGestionnaire() != null && personnel.getCreationConventionGestionnaire();
            case TemplateMail.CODE_GES_MODIF_AVENANT:
                return personnel.getModificationAvenantGestionnaire() != null && personnel.getModificationAvenantGestionnaire();
            case TemplateMail.CODE_GES_MODIF_CONVENTION:
                return personnel.getModificationConventionGestionnaire() != null && personnel.getModificationConventionGestionnaire();
            default:
                return false;
        }
    }

    public static class MailContext {
        private ConventionContext convention = new ConventionContext();
        private TuteurProContext tuteurPro = new TuteurProContext();
        private SignataireContext signataire = new SignataireContext();
        private ModifieParContext modifiePar = new ModifieParContext();
        private EtudiantContext etudiant = new EtudiantContext();
        private AvenantContext avenant = new AvenantContext();

        public MailContext() { }

        public MailContext(ApplicationBootstrap applicationBootstrap, Convention convention, Avenant avenant, Utilisateur userModif) {
            if (convention != null) {
                this.convention = new ConventionContext(applicationBootstrap, convention);
                this.tuteurPro = new TuteurProContext(convention.getContact(), convention.getStructure(), convention.getService());
                this.signataire = new SignataireContext(convention.getSignataire());
                this.etudiant = new EtudiantContext(convention.getEtudiant(), convention.getCourrielPersoEtudiant(), convention.getTelEtudiant());
            }
            if (avenant != null) {
                this.avenant = new AvenantContext(avenant);
            }
            this.modifiePar = new ModifieParContext(userModif);
        }

        public ConventionContext getConvention() {
            return convention;
        }

        public void setConvention(ConventionContext convention) {
            this.convention = convention;
        }

        public TuteurProContext getTuteurPro() {
            return tuteurPro;
        }

        public void setTuteurPro(TuteurProContext tuteurPro) {
            this.tuteurPro = tuteurPro;
        }

        public SignataireContext getSignataire() {
            return signataire;
        }

        public void setSignataire(SignataireContext signataire) {
            this.signataire = signataire;
        }

        public ModifieParContext getModifiePar() {
            return modifiePar;
        }

        public void setModifiePar(ModifieParContext modifiePar) {
            this.modifiePar = modifiePar;
        }

        public EtudiantContext getEtudiant() {
            return etudiant;
        }

        public void setEtudiant(EtudiantContext etudiant) {
            this.etudiant = etudiant;
        }

        public AvenantContext getAvenant() {
            return avenant;
        }

        public void setAvenant(AvenantContext avenant) {
            this.avenant = avenant;
        }

        public static class ConventionContext {
            private String numero;
            private String typeStage;
            private String paysAccueil;
            private String etape;
            private String sujet;
            private String fonction;
            private String dateDebut;
            private String dateFin;
            private String tempsTravail;
            private String tempsTravailComment;
            private String lien;
            private String elementModifs;

            public ConventionContext() { }

            public ConventionContext(ApplicationBootstrap applicationBootstrap, Convention convention) {
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

                this.numero = String.valueOf(convention.getId());
                this.typeStage = convention.getTypeConvention() != null ? convention.getTypeConvention().getLibelle() : null;
                this.paysAccueil = convention.getService() != null && convention.getService().getPays() != null ? convention.getService().getPays().getLib() : null;
                this.etape = convention.getEtape() != null ? convention.getEtape().getId().getCode() + " - " + convention.getEtape().getLibelle() : null;
                this.sujet = convention.getSujetStage();
                this.fonction = convention.getFonctionsEtTaches();
                this.dateDebut = convention.getDateDebutStage() != null ? df.format(convention.getDateDebutStage()) : null;
                this.dateFin = convention.getDateFinStage() != null ? df.format(convention.getDateFinStage()) : null;
                this.tempsTravail = convention.getTempsTravail() != null ? convention.getTempsTravail().getLibelle() : null;
                this.tempsTravailComment = convention.getCommentaireDureeTravail();
                this.lien = applicationBootstrap.getAppConfig().getUrl() + "/conventions/" + convention.getId();
            }

            public String getNumero() {
                return numero != null ? numero : "";
            }

            public void setNumero(String numero) {
                this.numero = numero;
            }

            public String getTypeStage() {
                return typeStage != null ? typeStage : "";
            }

            public void setTypeStage(String typeStage) {
                this.typeStage = typeStage;
            }

            public String getPaysAccueil() {
                return paysAccueil != null ? paysAccueil : "";
            }

            public void setPaysAccueil(String paysAccueil) {
                this.paysAccueil = paysAccueil;
            }

            public String getEtape() {
                return etape != null ? etape : "";
            }

            public void setEtape(String etape) {
                this.etape = etape;
            }

            public String getSujet() {
                return sujet != null ? sujet : "";
            }

            public void setSujet(String sujet) {
                this.sujet = sujet;
            }

            public String getFonction() {
                return fonction != null ? fonction : "";
            }

            public void setFonction(String fonction) {
                this.fonction = fonction;
            }

            public String getDateDebut() {
                return dateDebut != null ? dateDebut : "";
            }

            public void setDateDebut(String dateDebut) {
                this.dateDebut = dateDebut;
            }

            public String getDateFin() {
                return dateFin != null ? dateFin : "";
            }

            public void setDateFin(String dateFin) {
                this.dateFin = dateFin;
            }

            public String getTempsTravail() {
                return tempsTravail != null ? tempsTravail : "";
            }

            public void setTempsTravail(String tempsTravail) {
                this.tempsTravail = tempsTravail;
            }

            public String getTempsTravailComment() {
                return tempsTravailComment != null ? tempsTravailComment : "";
            }

            public void setTempsTravailComment(String tempsTravailComment) {
                this.tempsTravailComment = tempsTravailComment;
            }

            public String getLien() {
                return lien != null ? lien : "";
            }

            public void setLien(String lien) {
                this.lien = lien;
            }

            public String getElementModifs() {
                return elementModifs != null ? elementModifs : "";
            }

            public void setElementModifs(String elementModifs) {
                this.elementModifs = elementModifs;
            }
        }

        public static class TuteurProContext {
            private String nom;
            private String prenom;
            private String mail;
            private String tel;
            private String etabAccueil;
            private String serviceAccueil;
            private String fonction;

            public TuteurProContext() { }

            public TuteurProContext(Contact contact, Structure structure, org.esup_portail.esup_stage.model.Service service) {
                if (contact != null) {
                    this.nom = contact.getNom();
                    this.prenom = contact.getPrenom();
                    this.mail = contact.getMail();
                    this.tel = contact.getTel();
                    this.fonction = contact.getFonction();
                }
                this.etabAccueil = structure != null ? structure.getRaisonSociale() : null;
                this.serviceAccueil = service != null ? service.getNom() : null;
            }

            public String getNom() {
                return nom != null ? nom : "";
            }

            public void setNom(String nom) {
                this.nom = nom;
            }

            public String getPrenom() {
                return prenom != null ? prenom : "";
            }

            public void setPrenom(String prenom) {
                this.prenom = prenom;
            }

            public String getMail() {
                return mail != null ? mail : "";
            }

            public void setMail(String mail) {
                this.mail = mail;
            }

            public String getTel() {
                return tel != null ? tel : "";
            }

            public void setTel(String tel) {
                this.tel = tel;
            }

            public String getEtabAccueil() {
                return etabAccueil != null ? etabAccueil : "";
            }

            public void setEtabAccueil(String etabAccueil) {
                this.etabAccueil = etabAccueil;
            }

            public String getServiceAccueil() {
                return serviceAccueil != null ? serviceAccueil : "";
            }

            public void setServiceAccueil(String serviceAccueil) {
                this.serviceAccueil = serviceAccueil;
            }

            public String getFonction() {
                return fonction != null ? fonction : "";
            }

            public void setFonction(String fonction) {
                this.fonction = fonction;
            }
        }

        public static class SignataireContext {
            private String nom;
            private String prenom;
            private String mail;
            private String tel;
            private String fonction;

            public SignataireContext() { }

            public SignataireContext(Contact contact) {
                if (contact != null) {
                    this.nom = contact.getNom();
                    this.prenom = contact.getPrenom();
                    this.mail = contact.getMail();
                    this.tel = contact.getTel();
                    this.fonction = contact.getFonction();
                }
            }

            public String getNom() {
                return nom != null ? nom : "";
            }

            public void setNom(String nom) {
                this.nom = nom;
            }

            public String getPrenom() {
                return prenom != null ? prenom : "";
            }

            public void setPrenom(String prenom) {
                this.prenom = prenom;
            }

            public String getMail() {
                return mail != null ? mail : "";
            }

            public void setMail(String mail) {
                this.mail = mail;
            }

            public String getTel() {
                return tel != null ? tel : "";
            }

            public void setTel(String tel) {
                this.tel = tel;
            }

            public String getFonction() {
                return fonction != null ? fonction : "";
            }

            public void setFonction(String fonction) {
                this.fonction = fonction;
            }
        }

        public static class EtudiantContext {
            private String nom;
            private String prenom;
            private String mail;
            private String tel;

            public EtudiantContext() { }

            public EtudiantContext(Etudiant etudiant, String mailPerso, String tel) {
                this.nom = etudiant.getNom();
                this.prenom = etudiant.getPrenom();
                this.mail = mailPerso != null && !mailPerso.isEmpty() ? mailPerso : etudiant.getMail();
                this.tel = tel;
            }

            public String getNom() {
                return nom != null ? nom : "";
            }

            public void setNom(String nom) {
                this.nom = nom;
            }

            public String getPrenom() {
                return prenom != null ? prenom : "";
            }

            public void setPrenom(String prenom) {
                this.prenom = prenom;
            }

            public String getMail() {
                return mail != null ? mail : "";
            }

            public void setMail(String mail) {
                this.mail = mail;
            }

            public String getTel() {
                return tel != null ? tel : "";
            }

            public void setTel(String tel) {
                this.tel = tel;
            }
        }

        public static class ModifieParContext {
            private String nom;
            private String prenom;

            public ModifieParContext() { }

            public ModifieParContext(Utilisateur utilisateur) {
                this.nom = utilisateur.getNom();
                this.prenom = utilisateur.getPrenom();
            }

            public String getNom() {
                return nom != null ? nom : "";
            }

            public void setNom(String nom) {
                this.nom = nom;
            }

            public String getPrenom() {
                return prenom != null ? prenom : "";
            }

            public void setPrenom(String prenom) {
                this.prenom = prenom;
            }
        }

        public static class AvenantContext {
            private String numero;

            public AvenantContext() { }

            public AvenantContext(Avenant avenant) {
                this.numero = String.valueOf(avenant.getId());
            }

            public String getNumero() {
                return numero != null ? numero : "";
            }

            public void setNumero(String numero) {
                this.numero = numero;
            }
        }
    }
}
