package org.esup_portail.esup_stage.service;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.esup_portail.esup_stage.exception.AppException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires de FileValidationService.validatePdf(...) :
 * - acceptation des PDF sains (y compris avec OpenAction GoTo bénigne) ;
 * - rejet structurel des constructions dangereuses (JavaScript, Launch,
 *   fichiers intégrés, RichMedia, XFA) où qu'elles soient dans le graphe d'objets ;
 * - contrôles de base (taille, extension, type MIME, signature %PDF-).
 */
class FileValidationServiceTest {

    private FileValidationService service;

    @BeforeEach
    void setUp() {
        service = new FileValidationService();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // PDF sains : aucun faux positif attendu
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test
    void acceptePdfSain() {
        byte[] pdf = buildPdf(doc -> {});
        FileValidationService.ValidatedPdf validated = service.validatePdf(pdfFile(pdf), 10);
        assertThat(validated.contentType()).isEqualTo("application/pdf");
        assertThat(validated.bytes()).isEqualTo(pdf);
        assertThat(validated.sha256()).isNotBlank();
    }

    @Test
    void acceptePdfAvecOpenActionGoToBenigne() {
        // LaTeX/Word génèrent couramment une OpenAction GoTo (aller à la page 1) : ne doit pas être rejetée
        byte[] pdf = buildPdf(doc -> {
            PdfDictionary goTo = new PdfDictionary();
            goTo.put(PdfName.S, PdfName.GoTo);
            PdfArray destination = new PdfArray();
            destination.add(doc.getPage(1).getPdfObject());
            destination.add(PdfName.Fit);
            goTo.put(PdfName.D, destination);
            doc.getCatalog().getPdfObject().put(PdfName.OpenAction, goTo);
        });
        assertThatCode(() -> service.validatePdf(pdfFile(pdf), 10)).doesNotThrowAnyException();
    }

    @Test
    void acceptePdfDontLeFluxCompresseContientDesMarqueursFortuits() {
        // Reproduit le faux positif historique : les octets "/js" ou "/aa" apparaissant par hasard
        // dans un flux compressé (image, contenu) ne doivent plus déclencher de rejet
        byte[] noise = "abc/js$-9(xyz/aAP.e/launch-fortuit/aa/embeddedfile".getBytes(StandardCharsets.ISO_8859_1);
        byte[] pdf = buildPdf(doc -> {
            com.itextpdf.kernel.pdf.PdfStream stream = new com.itextpdf.kernel.pdf.PdfStream(noise);
            doc.getPage(1).getPdfObject().put(new PdfName("PieceInfo2"), stream.makeIndirect(doc));
        });
        assertThatCode(() -> service.validatePdf(pdfFile(pdf), 10)).doesNotThrowAnyException();
    }

    @Test
    void acceptLePdfReelSiFourni() throws IOException {
        // Test de non-régression sur un vrai PDF volumineux :
        // mvnw test -Dtest=FileValidationServiceTest -Dpdf.sample.path="C:\chemin\vers\fichier.pdf"
        String samplePath = System.getProperty("pdf.sample.path");
        Assumptions.assumeTrue(samplePath != null && Files.exists(Path.of(samplePath)),
                "Pas de PDF réel fourni via -Dpdf.sample.path : test ignoré");
        byte[] pdf = Files.readAllBytes(Path.of(samplePath));
        assertThatCode(() -> service.validatePdf(pdfFile(pdf), 10)).doesNotThrowAnyException();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constructions dangereuses : rejet structurel attendu
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test
    void rejetteJavaScriptDansOpenAction() {
        byte[] pdf = buildPdf(doc -> {
            PdfDictionary action = new PdfDictionary();
            action.put(PdfName.S, PdfName.JavaScript);
            action.put(PdfName.JS, new PdfString("app.alert(1);"));
            doc.getCatalog().getPdfObject().put(PdfName.OpenAction, action);
        });
        assertRejected(pdf, "JavaScript");
    }

    @Test
    void rejetteJavaScriptDansActionAdditionnellePage() {
        byte[] pdf = buildPdf(doc -> {
            PdfDictionary action = new PdfDictionary();
            action.put(PdfName.S, PdfName.JavaScript);
            action.put(PdfName.JS, new PdfString("this.print();"));
            PdfDictionary additionalActions = new PdfDictionary();
            additionalActions.put(PdfName.O, action);
            doc.getPage(1).getPdfObject().put(PdfName.AA, additionalActions);
        });
        assertRejected(pdf, "JavaScript");
    }

    @Test
    void rejetteArbreDeNomsJavaScript() {
        byte[] pdf = buildPdf(doc -> {
            PdfDictionary names = new PdfDictionary();
            names.put(PdfName.JavaScript, new PdfDictionary());
            doc.getCatalog().getPdfObject().put(PdfName.Names, names);
        });
        assertRejected(pdf, "JavaScript");
    }

    @Test
    void rejetteActionLaunch() {
        byte[] pdf = buildPdf(doc -> {
            PdfDictionary action = new PdfDictionary();
            action.put(PdfName.S, PdfName.Launch);
            action.put(PdfName.F, new PdfString("calc.exe"));
            doc.getCatalog().getPdfObject().put(PdfName.OpenAction, action);
        });
        assertRejected(pdf, "lancement de programme");
    }

    @Test
    void rejetteFichiersIntegres() {
        byte[] pdf = buildPdf(doc -> {
            PdfDictionary names = new PdfDictionary();
            names.put(PdfName.EmbeddedFiles, new PdfDictionary());
            doc.getCatalog().getPdfObject().put(PdfName.Names, names);
        });
        assertRejected(pdf, "fichier intégré");
    }

    @Test
    void rejetteAnnotationFileAttachment() {
        byte[] pdf = buildPdf(doc -> {
            PdfDictionary annotation = new PdfDictionary();
            annotation.put(PdfName.Subtype, PdfName.FileAttachment);
            annotation.put(PdfName.Rect, new PdfArray(new float[]{0, 0, 10, 10}));
            PdfArray annotations = new PdfArray();
            annotations.add(annotation);
            doc.getPage(1).getPdfObject().put(PdfName.Annots, annotations);
        });
        assertRejected(pdf, "fichier intégré");
    }

    @Test
    void rejetteAnnotationRichMedia() {
        byte[] pdf = buildPdf(doc -> {
            PdfDictionary annotation = new PdfDictionary();
            annotation.put(PdfName.Subtype, PdfName.RichMedia);
            annotation.put(PdfName.Rect, new PdfArray(new float[]{0, 0, 10, 10}));
            PdfArray annotations = new PdfArray();
            annotations.add(annotation);
            doc.getPage(1).getPdfObject().put(PdfName.Annots, annotations);
        });
        assertRejected(pdf, "média riche");
    }

    @Test
    void rejetteFormulaireXfa() {
        byte[] pdf = buildPdf(doc -> {
            PdfDictionary acroForm = new PdfDictionary();
            acroForm.put(PdfName.XFA, new PdfArray());
            doc.getCatalog().getPdfObject().put(PdfName.AcroForm, acroForm);
        });
        assertRejected(pdf, "XFA");
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Contrôles de base
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test
    void rejetteFichierAbsent() {
        assertThatThrownBy(() -> service.validatePdf(null, 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("aucun fichier");
    }

    @Test
    void rejetteFichierVide() {
        MockMultipartFile file = new MockMultipartFile("doc", "test.pdf", "application/pdf", new byte[0]);
        assertThatThrownBy(() -> service.validatePdf(file, 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("vide");
    }

    @Test
    void rejetteFichierTropVolumineux() {
        byte[] tooBig = new byte[2 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("doc", "test.pdf", "application/pdf", tooBig);
        assertThatThrownBy(() -> service.validatePdf(file, 1))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("limite autorisée est de 1 Mo");
    }

    @Test
    void rejetteMauvaiseExtension() {
        byte[] pdf = buildPdf(doc -> {});
        MockMultipartFile file = new MockMultipartFile("doc", "test.txt", "application/pdf", pdf);
        assertThatThrownBy(() -> service.validatePdf(file, 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(".pdf");
    }

    @Test
    void rejetteMauvaisTypeMime() {
        byte[] pdf = buildPdf(doc -> {});
        MockMultipartFile file = new MockMultipartFile("doc", "test.pdf", "text/plain", pdf);
        assertThatThrownBy(() -> service.validatePdf(file, 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("type MIME");
    }

    @Test
    void rejetteFauxPdfRenomme() {
        byte[] notAPdf = "ceci n'est pas un pdf".getBytes(StandardCharsets.ISO_8859_1);
        MockMultipartFile file = new MockMultipartFile("doc", "test.pdf", "application/pdf", notAPdf);
        assertThatThrownBy(() -> service.validatePdf(file, 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("%PDF-");
    }

    @Test
    void rejettePdfTronque() {
        byte[] pdf = buildPdf(doc -> {});
        byte[] truncated = new byte[pdf.length / 2];
        System.arraycopy(pdf, 0, truncated, 0, truncated.length);
        assertThatThrownBy(() -> service.validatePdf(pdfFile(truncated), 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("%%EOF");
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Helpers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private byte[] buildPdf(Consumer<PdfDocument> customizer) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfDocument doc = new PdfDocument(new PdfWriter(out))) {
            doc.addNewPage();
            customizer.accept(doc);
        }
        return out.toByteArray();
    }

    private MockMultipartFile pdfFile(byte[] bytes) {
        return new MockMultipartFile("doc", "test.pdf", "application/pdf", bytes);
    }

    private void assertRejected(byte[] pdf, String expectedMessagePart) {
        assertThatThrownBy(() -> service.validatePdf(pdfFile(pdf), 10))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("PDF refusé")
                .hasMessageContaining(expectedMessagePart)
                .extracting(e -> ((AppException) e).getHttpStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
