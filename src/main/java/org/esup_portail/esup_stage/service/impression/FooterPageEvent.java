package org.esup_portail.esup_stage.service.impression;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FooterPageEvent implements IEventHandler {
    protected Date dateGeneration;
    protected SimpleDateFormat simpleDateFormat;

    protected PdfFormXObject placeholder;
    protected float side = 20;
    protected float x = 330;
    protected float y = 10;
    protected float space = 4.5f;
    protected float descent = 3;

    public FooterPageEvent(Date dateGeneration) {
        this.dateGeneration = dateGeneration;
        this.simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        placeholder = new PdfFormXObject(new Rectangle(0, 0, side, side));
    }

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdf = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        Rectangle pageSize = page.getPageSize();
        PdfCanvas pdfCanvas = new PdfCanvas(page.getLastContentStream(), page.getResources(), pdf);
        Canvas canvas = new Canvas(pdfCanvas, pageSize);
        Paragraph p = new Paragraph();
        p.setFontSize(8).add(simpleDateFormat.format(dateGeneration));
        canvas.showTextAligned(p, x, y, TextAlignment.RIGHT);
        pdfCanvas.addXObjectAt(placeholder, x + space, y - descent);
        pdfCanvas.release();
    }

}
