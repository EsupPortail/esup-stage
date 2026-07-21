package org.esup_portail.esup_stage.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.filespec.PdfFileSpec;
import com.itextpdf.kernel.pdf.filespec.PdfStringFS;
import org.esup_portail.esup_stage.exception.AppException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileValidationServiceValidationTest {

    private final FileValidationService service = new FileValidationService();

    private byte[] imagePng() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return os.toByteArray();
    }

    private byte[] imageJpeg() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", os);
        return os.toByteArray();
    }

    private byte[] pdfValide() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfDocument document = new PdfDocument(new PdfWriter(os));
        document.addNewPage();
        document.close();
        return os.toByteArray();
    }

    private byte[] pdfAvec(java.util.function.Consumer<PdfDocument> personnalisation) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfDocument document = new PdfDocument(new PdfWriter(os));
        document.addNewPage();
        personnalisation.accept(document);
        document.close();
        return os.toByteArray();
    }

    private MockMultipartFile pdf(String nom, String contentType, byte[] bytes) {
        return new MockMultipartFile("file", nom, contentType, bytes);
    }

    // ------------------------------------------------------------------
    // images
    // ------------------------------------------------------------------

    @Test
    void lesImagesPngEtJpegSontAcceptees() throws Exception {
        FileValidationService.ValidatedImage png = service.validateImage(
                new MockMultipartFile("logo", "logo.png", "image/png", imagePng()));
        assertThat(png.contentType()).isEqualTo("image/png");
        assertThat(png.extension()).isEqualTo("png");

        FileValidationService.ValidatedImage jpeg = service.validateImage(
                new MockMultipartFile("logo", "logo.jpg", "image/jpeg", imageJpeg()));
        assertThat(jpeg.contentType()).isEqualTo("image/jpeg");
        assertThat(jpeg.extension()).isEqualTo("jpg");
    }

    @Test
    void lesFichiersNonImageSontRefuses() {
        assertThatThrownBy(() -> service.validateImage(null)).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> service.validateImage(new MockMultipartFile("logo", new byte[0])))
                .isInstanceOf(AppException.class);
        assertThatThrownBy(() -> service.validateImage(
                new MockMultipartFile("logo", "logo.png", "image/png", "pas une image".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("format image");
    }

    // ------------------------------------------------------------------
    // PDF
    // ------------------------------------------------------------------

    @Test
    void unVraiPdfEstAccepteAvecSonEmpreinte() {
        FileValidationService.ValidatedPdf valide = service.validatePdf(
                pdf("convention.pdf", "application/pdf", pdfValide()), 10);

        assertThat(valide.contentType()).isEqualTo("application/pdf");
        assertThat(valide.extension()).isEqualTo("pdf");
        assertThat(valide.sha256()).hasSize(64);
        assertThat(valide.bytes()).isNotEmpty();
    }

    @Test
    void lesControlesDeSurfaceRejettentLesMauvaisFichiers() {
        assertThatThrownBy(() -> service.validatePdf(null, 10))
                .isInstanceOf(AppException.class).hasMessageContaining("aucun fichier");
        assertThatThrownBy(() -> service.validatePdf(pdf("a.pdf", "application/pdf", new byte[0]), 10))
                .isInstanceOf(AppException.class).hasMessageContaining("vide");
        assertThatThrownBy(() -> service.validatePdf(pdf("a.txt", "application/pdf", pdfValide()), 10))
                .isInstanceOf(AppException.class).hasMessageContaining(".pdf");
        assertThatThrownBy(() -> service.validatePdf(pdf("a.pdf", "text/plain", pdfValide()), 10))
                .isInstanceOf(AppException.class).hasMessageContaining("type MIME");
        assertThatThrownBy(() -> service.validatePdf(pdf("a.pdf", null, pdfValide()), 10))
                .isInstanceOf(AppException.class).hasMessageContaining("absent");
    }

    @Test
    void laTailleMaximaleEstRespectee() {
        byte[] tropGros = new byte[(int) (1.5 * 1024 * 1024)];
        assertThatThrownBy(() -> service.validatePdf(pdf("gros.pdf", "application/pdf", tropGros), 1))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("limite autorisée est de 1 Mo");
    }

    @Test
    void laSignatureEtLaFinDeFichierSontVerifiees() {
        assertThatThrownBy(() -> service.validatePdf(
                pdf("faux.pdf", "application/pdf", "pas un pdf du tout".getBytes(StandardCharsets.UTF_8)), 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("%PDF-");

        assertThatThrownBy(() -> service.validatePdf(
                pdf("tronque.pdf", "application/pdf", "%PDF-1.4 contenu sans fin".getBytes(StandardCharsets.UTF_8)), 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("%%EOF");
    }

    @Test
    void lesElementsActifsSontInterdits() {
        // La détection repose désormais sur l'inspection structurelle du graphe d'objets PDF :
        // les scénarios doivent être de vrais PDF (iText) portant les constructions interdites.
        byte[] pdfJs = pdfAvec(document ->
                document.getCatalog().setOpenAction(PdfAction.createJavaScript("app.alert('x');")));
        assertThatThrownBy(() -> service.validatePdf(pdf("js.pdf", "application/pdf", pdfJs), 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("JavaScript");

        byte[] pdfLaunch = pdfAvec(document ->
                document.getCatalog().setOpenAction(PdfAction.createLaunch(new PdfStringFS("calc.exe"))));
        assertThatThrownBy(() -> service.validatePdf(pdf("launch.pdf", "application/pdf", pdfLaunch), 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("lancement de programme");

        byte[] pdfEmbedded = pdfAvec(document -> document.addFileAttachment("virus.txt",
                PdfFileSpec.createEmbeddedFileSpec(document, "contenu".getBytes(StandardCharsets.ISO_8859_1),
                        "piece jointe", "virus.txt", null, null, null)));
        assertThatThrownBy(() -> service.validatePdf(pdf("embed.pdf", "application/pdf", pdfEmbedded), 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("fichier intégré");
    }

    @Test
    void unPdfCorrompuEstSignaleCommeInvalide() {
        String corrompu = "%PDF-1.4\ncontenu invalide sans structure\n%%EOF";
        assertThatThrownBy(() -> service.validatePdf(
                pdf("corrompu.pdf", "application/pdf", corrompu.getBytes(StandardCharsets.ISO_8859_1)), 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("PDF refusé");
    }
}
