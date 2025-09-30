package org.esup_portail.esup_stage.service.impression;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.enums.FolderEnum;
import org.esup_portail.esup_stage.enums.TypeSignatureEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.QuestionSupplementaireJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateConventionJpaRepository;
import org.esup_portail.esup_stage.service.impression.context.ImpressionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImpressionService {
    private static final Logger logger = LogManager.getLogger(ImpressionService.class);

    @Autowired
    TemplateConventionJpaRepository templateConventionJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    AppliProperties appliProperties;

    @Autowired
    QuestionSupplementaireJpaRepository QSJpaRepository;

    //    @Autowired
    //    ConventionService conventionService;
    //
    //    @Autowired
    //    ConventionJpaRepository conventionJpaRepository;

    public void generateConventionAvenantPDF(Convention convention, Avenant avenant, ByteArrayOutputStream ou, boolean isRecap) {
        if (convention.getNomenclature() == null) {
            convention.setValeurNomenclature();
        }
        TemplateConvention templateConvention = templateConventionJpaRepository.findByTypeAndLangue(convention.getTypeConvention().getId(), convention.getLangueConvention().getCode());
        if (templateConvention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Template convention " + convention.getTypeConvention().getLibelle() + "-" + convention.getLangueConvention().getCode() + " non trouvé");
        }
        CentreGestion centreEtablissement = centreGestionJpaRepository.getCentreEtablissement();
        List<QuestionSupplementaire> questionSupplementaire = QSJpaRepository.findByFicheEvaluation(centreEtablissement.getFicheEvaluation().getId());
        ImpressionContext impressionContext = new ImpressionContext(convention, avenant, centreEtablissement, questionSupplementaire);

        try {

            String htmlTexte = avenant != null ? this.getHtmlText(templateConvention.getTexteAvenant(), false, isRecap) : this.getHtmlText(templateConvention.getTexte(), true, isRecap);

            htmlTexte = manageIfElse(htmlTexte);

            Configuration freeMarkerConfig = freeMarkerConfigurer.getConfiguration();
            freeMarkerConfig.setClassicCompatible(true);
            Template template = new Template("template_convention_texte" + templateConvention.getId(), htmlTexte, freeMarkerConfig);
            StringWriter texte = new StringWriter();
            template.process(impressionContext, texte);

            String filename = avenant != null ? "avenant_" + avenant.getId() : "convention_" + convention.getId();
            filename += "_" + convention.getEtudiant().getPrenom() + "_" + convention.getEtudiant().getNom() + ".pdf";

            // récupération du logo du centre gestion
            String logoname;
            Fichier fichier = convention.getCentreGestion().getFichier();
            ImageData imageData = null;

            if (fichier != null) {
                logoname = this.getLogoFilePath(this.getNomFichier(fichier.getId(), fichier.getNom()));
                if (Files.exists(Paths.get(logoname))) {
                    imageData = ImageDataFactory.create(logoname);
                }
            }

            // si le centre de gestion n'a pas de logo ou qu'il n'existe pas physiquement, on prend celui du centre établissement
            if (imageData == null) {
                fichier = centreEtablissement.getFichier();
                if (fichier != null) {
                    logoname = this.getLogoFilePath(this.getNomFichier(fichier.getId(), fichier.getNom()));
                    if (Files.exists(Paths.get(logoname))) {
                        imageData = ImageDataFactory.create(logoname);
                    }
                }
            }

            this.generatePDF(texte.toString(), filename, imageData, ou,false);
        } catch (Exception e) {
            logger.error("Une erreur est survenue lors de la génération du PDF", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur technique");
        }
    }

    public void generateFichePDF(String htmlTexte, ByteArrayOutputStream ou) {
        try {
            String filename = "FicheEtudiant.pdf";
            this.generatePDF(htmlTexte, filename, null, ou,false);
        } catch (Exception e) {
            logger.error("Une erreur est survenue lors de la génération du PDF", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur technique");
        }
    }

    public void generatePDF(String texte, String filename, ImageData imageData, ByteArrayOutputStream ou, boolean isEvaluation) {
        String tempFilePath = this.getClass().getResource("/templates").getPath();
        String tempFile = tempFilePath + "temp_" + filename;
        FileOutputStream fop = null;
        Date dateGeneration = new Date();
        try {
            fop = new FileOutputStream(tempFile);
            HtmlConverter.convertToPdf(texte, fop);
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(tempFile), new PdfWriter(ou));
            FooterPageEvent event = new FooterPageEvent(dateGeneration);
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, event);
            Document document = new Document(pdfDoc);

            if (imageData != null) {
                Image img = prepareLogoImage(imageData, isEvaluation);
                document.add(img);
            }
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    File file = new File(tempFile);
                    fop.close();
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getDefaultText(boolean isConvention) {
        String templateName = isConvention ? "/templates/template_default_convention.html" : "/templates/template_default_avenant.html";
        return getDefaultText(templateName);
    }

    public String getDefaultText(String templateName) {
        StringBuilder sb = new StringBuilder();
        String str;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(templateName)));
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
            in.close();

            return sb.toString();
        } catch (Exception e) {
            throw new AppException(HttpStatus.NOT_FOUND, "Template par défaut non trouvé");
        }
    }

    public String generateXmlData(Convention convention, TypeSignatureEnum typeSignatureEnum) {
        List<Map<String, String>> otp = new ArrayList<>();
        List<CentreGestionSignataire> signatairesOtp = convention.getCentreGestion().getSignataires().stream().filter(s -> s.getType() == typeSignatureEnum).collect(Collectors.toList());
        if (signatairesOtp.size() == 0) {
            return null;
        }
        for (CentreGestionSignataire signataire : signatairesOtp) {
            switch (signataire.getId().getSignataire()) {
                case etudiant:
                    // Ajout de l'étudiant
                    otp.add(new HashMap<>() {{
                        put("firstname", convention.getEtudiant().getPrenom());
                        put("lastname", convention.getEtudiant().getNom());
                        put("phoneNumber", getOtpDataPhoneNumber(convention.getTelPortableEtudiant()));
                        put("email", getOtpDataEmail(convention.getEtudiant().getMail()));
                    }});
                    break;
                case enseignant:
                    // Ajout de l'enseignant référent
                    otp.add(new HashMap<>() {{
                        put("firstname", convention.getEnseignant().getPrenom());
                        put("lastname", convention.getEnseignant().getNom());
                        put("phoneNumber", getOtpDataPhoneNumber(convention.getEnseignant().getTel()));
                        put("email", getOtpDataEmail(convention.getEnseignant().getMail()));
                    }});
                    break;
                case tuteur:
                    // Ajout du tuteur pédagogique
                    otp.add(new HashMap<>() {{
                        put("firstname", convention.getContact().getPrenom());
                        put("lastname", convention.getContact().getNom());
                        put("phoneNumber", getOtpDataPhoneNumber(convention.getContact().getTel()));
                        put("email", getOtpDataEmail(convention.getContact().getMail()));
                    }});
                    break;
                case signataire:
                    // Ajout du signataire de la convention
                    otp.add(new HashMap<>() {{
                        put("firstname", convention.getSignataire().getPrenom());
                        put("lastname", convention.getSignataire().getNom());
                        put("phoneNumber", getOtpDataPhoneNumber(convention.getSignataire().getTel()));
                        put("email", getOtpDataEmail(convention.getSignataire().getMail()));
                    }});
                    break;
                case viseur:
                    // Ajout du directeur du département
                    otp.add(new HashMap<>() {{
                        put("firstname", convention.getCentreGestion().getPrenomViseur());
                        put("lastname", convention.getCentreGestion().getNomViseur());
                        put("phoneNumber", getOtpDataPhoneNumber(convention.getCentreGestion().getTelephone()));
                        put("email", getOtpDataEmail(convention.getCentreGestion().getMail()));
                    }});
                    break;
                default:
                    break;
            }
        }

        // Construction du xml
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<meta-data-list>");
        if (typeSignatureEnum == TypeSignatureEnum.otp) {
            for (int i = 0; i < otp.size(); ++i) {
                sb.append("<meta-data name=\"OTP_firstname_").append(i).append("\" value=\"").append(otp.get(i).get("firstname")).append("\"/>");
                sb.append("<meta-data name=\"OTP_lastname_").append(i).append("\" value=\"").append(otp.get(i).get("lastname")).append("\"/>");
                sb.append("<meta-data name=\"OTP_phonenumber_").append(i).append("\" value=\"").append(otp.get(i).get("phoneNumber")).append("\"/>");
                sb.append("<meta-data name=\"OTP_email_").append(i).append("\" value=\"").append(otp.get(i).get("email")).append("\"/>");
            }
        } else {
            int counter = 1;
            for (String key : Arrays.asList("lastname", "firstname", "email")) {
                for (Map<String, String> stringStringMap : otp) {
                    sb.append("<meta-data name=\"TEXT").append(String.format("%03d", counter++)).append("\" value=\"").append(stringStringMap.get(key)).append("\"/>");
                }
            }
        }
        sb.append("</meta-data-list>");
        return sb.toString();
    }

    private String getHtmlText(String texte, boolean isConvention, boolean isRecap) {

        if (texte == null) {
            texte = getDefaultText(isConvention);
        }

        String htmlTexte = getDefaultText("/templates/template_style.html");
        htmlTexte = htmlTexte.replaceAll("__project_fonts_dir__", this.getClass().getResource("/static/fonts/").getPath());

        if (isRecap) {
            htmlTexte += getDefaultText("/templates/template_recapitulatif.html");
        } else {
            String periodesInterruptionsStage = getDefaultText("/templates/template_convention_periodesInterruptions.html");
            texte = texte.replace("${convention.periodesInterruptions}", periodesInterruptionsStage);

            String horaireIrregulier = getDefaultText("/templates/template_convention_horaireIrregulier.html");
            texte = texte.replace("${convention.horaireIrregulier}", horaireIrregulier);

            // Remplacement ${avenant.motifs} par le template html contenant tous les motifs
            String motifTexte = getDefaultText("/templates/template_avenant_motifs.html");
            texte = texte.replace("${avenant.motifs}", motifTexte);

            // Remplacement ${avenant.contact} par le bon signataire (nom & prénom) si il a ete change
            String avenantContact = getDefaultText("/templates/template_avenant_contact.html");
            texte = texte.replace("${avenant.contact}", avenantContact);

            // Remplacement ${avenant.enseignant} par le bon signataire (nom & prénom) si il a ete change
            String avenantEnseignant = getDefaultText("/templates/template_avenant_enseignant.html");
            texte = texte.replace("${avenant.enseignant}", avenantEnseignant);

            // Style par défaut des tables dans les templates
            htmlTexte += texte;
        }
        // Remplacement de tags et styles générés par l'éditeur qui ne sont pas convertis correctement
        htmlTexte = htmlTexte.replace("<figure", "<div");
        htmlTexte = htmlTexte.replace("</figure>", "</div>");

        return htmlTexte;
    }

    private String getLogoFilePath(String filename) {
        return appliProperties.getDataDir() + FolderEnum.CENTRE_GESTION_LOGOS + "/" + filename;
    }

    private String getNomFichier(int idFichier, String nomFichier) {
        return idFichier + "_" + nomFichier;
    }

    public String getOtpDataPhoneNumber(String phoneNumber) {
        String deliveryAddress = appliProperties.getMailer().getDeliveryAddress();
        if (deliveryAddress != null && !deliveryAddress.isEmpty()  && !deliveryAddress.equals("null")) {
            return "";
        }
        return phoneNumber;
    }

    public String getOtpDataEmail(String email) {
        String deliveryAddress = appliProperties.getMailer().getDeliveryAddress();
        if (deliveryAddress != null && !deliveryAddress.isEmpty() && !deliveryAddress.equals("null")) {
            return deliveryAddress;
        }
        return email != null ? email : "";
    }

    public String manageIfElse(String text) {
        return text.replaceAll("\\$IF", "<#if")
                .replaceAll("\\$EQUALS ", "==\\\"")
                .replaceAll(" \\$FI", ">")
                .replaceAll("\\$ELSE", "<#else>")
                .replaceAll("\\?\\?\\$", "\\?\\?>")
                .replaceAll("\\$ENDIF", "</#if>");

    }

    /**
     * Prépare l'image du logo pour l'ajout dans le PDF.
     *
     * @param imageData les données de l'image
     * @return l'image prête à être ajoutée au document
     */
    private Image prepareLogoImage(ImageData imageData, boolean isEvaluation) {
        Image img = new Image(imageData);

        float maxWidth = 155f;
        float maxHeight = 75f;

        float width = img.getImageWidth();
        float height = img.getImageHeight();

        float widthRatio = maxWidth / width;
        float heightRatio = maxHeight / height;
        float scale = Math.min(1f, Math.min(widthRatio, heightRatio));

        img.scale(scale, scale);

        if (isEvaluation) {
            img.setHorizontalAlignment(HorizontalAlignment.CENTER);
        } else {
            img.setHorizontalAlignment(HorizontalAlignment.LEFT);
        }


        img.setMarginBottom(10f);
        return img;
    }

    /**
     * Génère le pdf de l'évaluation du tuteur de stage
     * @param convention
     * @param avenant
     * @param outputStream
     */
    public void generateEvaluationPDF(Convention convention, Avenant avenant, ByteArrayOutputStream outputStream, Integer typeRole) {
        if (convention.getNomenclature() == null) {
            convention.setValeurNomenclature();
        }
        String templatePath = getTemplatePath(convention, typeRole);

        CentreGestion centreEtablissement = centreGestionJpaRepository.getCentreEtablissement();
        List<QuestionSupplementaire> questionSupplementaire = QSJpaRepository.findByFicheEvaluation(centreEtablissement.getFicheEvaluation().getId());
        ImpressionContext impressionContext = new ImpressionContext(convention, avenant, centreEtablissement, questionSupplementaire);

        try {
            // Récupération du texte HTML directement depuis les fichiers
            String htmlTexte = getDefaultText(templatePath);
            htmlTexte = this.getHtmlText(htmlTexte,typeRole);

            // Traitement des conditions if/else comme dans generateConventionAvenantPDF
            htmlTexte = manageIfElse(htmlTexte);

            // Configuration FreeMarker
            Configuration freeMarkerConfig = freeMarkerConfigurer.getConfiguration();
            freeMarkerConfig.setClassicCompatible(true);
            String name = "";

            Template template = new Template("template_"+name, htmlTexte, freeMarkerConfig);

            // Remplir le template avec les données
            StringWriter texte = new StringWriter();
            template.process(impressionContext, texte);

            String filename = name + "_" + convention.getEtudiant().getPrenom() + "_" + convention.getEtudiant().getNom() + ".pdf";

            // Récupération du logo du centre gestion (même logique que generateConventionAvenantPDF)
            String logoname;
            Fichier fichier = convention.getCentreGestion().getFichier();
            ImageData imageData = null;

            if (fichier != null) {
                logoname = this.getLogoFilePath(this.getNomFichier(fichier.getId(), fichier.getNom()));
                if (Files.exists(Paths.get(logoname))) {
                    imageData = ImageDataFactory.create(logoname);
                }
            }

            // Si le centre de gestion n'a pas de logo ou qu'il n'existe pas physiquement, on prend celui du centre établissement
            if (imageData == null) {
                fichier = centreEtablissement.getFichier();
                if (fichier != null) {
                    logoname = this.getLogoFilePath(this.getNomFichier(fichier.getId(), fichier.getNom()));
                    if (Files.exists(Paths.get(logoname))) {
                        imageData = ImageDataFactory.create(logoname);
                    }
                }
            }

            // Génération du PDF
            this.generatePDF(texte.toString(), filename, imageData, outputStream,true);

        } catch (Exception e) {
            logger.error("Une erreur est survenue lors de la génération du PDF d'évaluation du tuteur", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur technique");
        }
    }

    private static String getTemplatePath(Convention convention, Integer typeRole) {
        String name = switch (typeRole) {
            case 0 -> "evaluation_etu";
            case 1 -> "evaluation_ens";
            case 2 -> "evaluation_tuteur";
            default -> throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Template non trouvée");
        };
        // Récupération du template d'évaluation depuis les fichiers
        String templatePath = "/templates/template_"+name;
        if (convention.getLangueConvention() != null && !convention.getLangueConvention().getCode().equals("fr")) {
            templatePath += "_" + convention.getLangueConvention().getCode();
        }
        templatePath += ".html";
        return templatePath;
    }

    private String getHtmlText(String texte, Integer typeRole) {
        String htmlTexte = getDefaultText("/templates/template_style.html");
        htmlTexte = htmlTexte.replaceAll("__project_fonts_dir__", this.getClass().getResource("/static/fonts/").getPath());

        htmlTexte += texte;

        htmlTexte = htmlTexte.replace("<figure", "<div");
        htmlTexte = htmlTexte.replace("</figure>", "</div>");

        return htmlTexte;
    }
}
