package org.esup_portail.esup_stage.service.impression;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import freemarker.template.Template;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateConventionJpaRepository;
import org.esup_portail.esup_stage.service.impression.context.ImpressionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.*;

@Service
public class ImpressionService {
    private static final Logger logger	= LogManager.getLogger(ImpressionService.class);

    @Autowired
    TemplateConventionJpaRepository templateConventionJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    public void generateConventionAvenantPDF(Convention convention, Avenant avenant, ByteArrayOutputStream ou) {
        TemplateConvention templateConvention = templateConventionJpaRepository.findByTypeAndLangue(convention.getTypeConvention().getId(), convention.getLangueConvention().getCode());
        if (templateConvention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Template convention " + convention.getTypeConvention().getLibelle() + "-" + convention.getLangueConvention().getCode() + " non trouvé");
        }

        ImpressionContext impressionContext = new ImpressionContext(convention, avenant);

        try {
            String htmlTexte = avenant != null ? this.getHtmlText(templateConvention.getTexteAvenant()) : this.getHtmlText(templateConvention.getTexte());

            Template template = new Template("template_convention_texte" + templateConvention.getId(), htmlTexte, freeMarkerConfigurer.getConfiguration());
            StringWriter texte = new StringWriter();
            template.process(impressionContext, texte);

            String filename = avenant != null ? "avenant_" + avenant.getId() : "convention_" + convention.getId();
            filename += "_" + convention.getEtudiant().getPrenom() + "_" + convention.getEtudiant().getNom() + ".pdf";

            // récupération du logo du centre gestion
            String logoname;
            Fichier fichier = convention.getCentreGestion().getFichier();
            ImageData imageData;

            // si le centre de gestion n'a pas de logo, on prend celui du centre établissement
            if (fichier == null) {
                CentreGestion centreEtablissement = centreGestionJpaRepository.getCentreEtablissement();
                fichier = centreEtablissement.getFichier();
            }

            logoname = this.getNomFichier(fichier.getId(), fichier.getNom());
            imageData = ImageDataFactory.create(this.getLogoFilePath(logoname));

            this.generatePDF(texte.toString(), filename, imageData, ou);
        } catch (Exception e) {
            logger.error("Une erreur est survenue lors de la génération du PDF", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur technique");
        }
    }

    public void generatePDF(String texte, String filename, ImageData imageData, ByteArrayOutputStream ou) {
        String tempFilePath = this.getClass().getResource("/templates").getPath();
        String tempFile = tempFilePath + "temp_" + filename;
        FileOutputStream fop = null;
        try {
            fop = new FileOutputStream(tempFile);
            HtmlConverter.convertToPdf(texte, fop);
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(tempFile), new PdfWriter(ou));
            Document document=new Document(pdfDoc);

            Image img = new Image(imageData);
            if (img.getImageWidth()>240) img.setWidth(240);
            document.add(img);
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

    private String getHtmlText(String texte) {
        // Style par défaut des tables dans les templates
        String htmlTexte = "<style>table { table-layout: fixed; width: 100%; overflow-wrap: break-word; border-spacing: 0px; }</style>";
        htmlTexte += texte;

        // Remplacement de tags et styles générés par l'éditeur qui ne sont pas convertis correctement
        htmlTexte = htmlTexte.replace("<figure", "<div");
        htmlTexte = htmlTexte.replace("</figure>", "</div>");
        htmlTexte = htmlTexte.replace("class=\"text-tiny\"", "style=\"font-size: 11px\"");
        htmlTexte = htmlTexte.replace("class=\"text-small\"", "style=\"font-size: 13px\"");
        htmlTexte = htmlTexte.replace("class=\"text-big\"", "style=\"font-size: 21px\"");
        htmlTexte = htmlTexte.replace("class=\"text-huge\"", "style=\"font-size: 23px\"");

        return htmlTexte;
    }

    private String getLogoFilePath(String filename) {
        return applicationBootstrap.getAppConfig().getDataDir() + "/centregestion/logos/" + filename;
    }

    private String getNomFichier(int idFichier, String nomFichier) {
        return idFichier + "_" + nomFichier;
    }
}
