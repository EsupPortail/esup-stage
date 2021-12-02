package org.esup_portail.esup_stage.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DocumentService {

    public void generatePDFTest() {
        // on sauvegarde le fichier
        File file;
        FileOutputStream fop = null;
        String filename = "test_logo.pdf";
        String filename_logo = "test_logo.pdf";
        try {
            file = new File(filename);
            fop = new FileOutputStream(file);
            try {
                generateListeCandidatsAvis(fop, "Formation de test", "Statut", "2021", "Rang");

                PdfDocument pdfDoc = new PdfDocument(new PdfReader(filename), new PdfWriter(filename_logo));
                Document document=new Document(pdfDoc);

                //HtmlConverter.convertToPdf(new File(src), new File(dest));

                ImageData data = ImageDataFactory.create("logo_dauphine.png");
                Image img = new Image(data);
                if (img.getImageWidth()>240) img.setWidth(240);
                document.add(img);
                document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {

                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void generateListeCandidatsAvis(OutputStream outputStream, String formation, String statutTexte, String campagneAnnee, String libRang) {

        // Remplissage du template Odt
        InputStream in = this.getClass().getResourceAsStream("/templates/Test.odt");
        try {
            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Freemarker);
            Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.ODFDOM);
            IContext ctx = report.createContext();
            ctx.put("formation", formation);
            ctx.put("statut", statutTexte);
            ctx.put("annee", campagneAnnee);
            ctx.put("libRang", libRang);
            report.convert(ctx, options, outputStream);

        } catch (IOException | XDocReportException e) {
            e.printStackTrace();
        }
    }
}
