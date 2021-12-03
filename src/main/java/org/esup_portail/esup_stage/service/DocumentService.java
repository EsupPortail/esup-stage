package org.esup_portail.esup_stage.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DocumentService {

    public void generatePDFTest() {
        String filename = "test.pdf";
        String filename_logo = "test_logo.pdf";
        try {
            String test = "";
            HtmlConverter.convertToPdf(test, new FileOutputStream(filename));
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(filename), new PdfWriter(filename_logo));
            Document document=new Document(pdfDoc);

            ImageData data = ImageDataFactory.create("logo_dauphine.png");
            Image img = new Image(data);
            if (img.getImageWidth()>240) img.setWidth(240);
            document.add(img);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
