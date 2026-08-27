package org.esup_portail.esup_stage.service;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfReader;
import org.apache.commons.codec.digest.DigestUtils;
import org.esup_portail.esup_stage.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

@Service
public class FileValidationService {

    private static final long MAX_PROPERTIES_SIZE_BYTES = 64L * 1024L;

    public ValidatedImage validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Le fichier doit être au format image");
        }

        try {
            byte[] bytes = file.getBytes();
            ImageType imageType = detectImageType(bytes);
            return new ValidatedImage(bytes, imageType.contentType(), imageType.extension());
        } catch (AppException e) {
            throw e;
        } catch (IOException e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Le fichier doit être au format image");
        }
    }

    private ImageType detectImageType(byte[] bytes) throws IOException {
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (imageInputStream == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Le fichier doit être au format image");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Le fichier doit être au format image");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInputStream, true, true);
                reader.getWidth(0);
                reader.getHeight(0);
                return ImageType.fromFormat(reader.getFormatName());
            } finally {
                reader.dispose();
            }
        }
    }

    public ValidatedPdf validatePdf(MultipartFile file, int maxSizeMo) {
        if (file == null) {
            throw rejectedPdf("aucun fichier n'a été transmis.");
        }
        if (file.isEmpty()) {
            throw rejectedPdf("le fichier transmis est vide.");
        }

        int effectiveMaxSizeMo = maxSizeMo > 0 ? maxSizeMo : 10;
        long maxSizeBytes = effectiveMaxSizeMo * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw rejectedPdf("la taille du fichier est de " + formatSize(file.getSize())
                    + ", alors que la limite autorisée est de " + effectiveMaxSizeMo + " Mo.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw rejectedPdf("l'extension du fichier doit être .pdf.");
        }

        String contentType = file.getContentType();
        if (!"application/pdf".equalsIgnoreCase(contentType)) {
            throw rejectedPdf("le type MIME reçu est " + readableContentType(contentType)
                    + ", alors que le type attendu est application/pdf.");
        }

        try {
            byte[] bytes = file.getBytes();
            validatePdfSignature(bytes);
            validatePdfContent(bytes);
            return new ValidatedPdf(bytes, "application/pdf", "pdf", DigestUtils.sha256Hex(bytes));
        } catch (AppException e) {
            throw e;
        } catch (IOException e) {
            throw rejectedPdf("le fichier n'a pas pu être lu correctement. Il est peut-être incomplet ou corrompu.");
        }
    }

    /**
     * Valide un fichier de libellés au format {@code .properties} déposé par un administrateur.
     * Le fichier doit être encodé en UTF-8, ne contenir que des clés connues de l'application et
     * des valeurs en texte pur : ces valeurs sont insérées telles quelles dans le HTML converti en PDF.
     *
     * @param clesAutorisees clés reconnues par l'application
     */
    public ValidatedProperties validateProperties(MultipartFile file, Set<String> clesAutorisees) {
        if (file == null) {
            throw rejectedProperties("aucun fichier n'a été transmis.");
        }
        if (file.isEmpty()) {
            throw rejectedProperties("le fichier transmis est vide.");
        }
        if (file.getSize() > MAX_PROPERTIES_SIZE_BYTES) {
            throw rejectedProperties("la taille du fichier est de " + formatSize(file.getSize())
                    + ", alors que la limite autorisée est de " + formatSize(MAX_PROPERTIES_SIZE_BYTES) + ".");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase(Locale.ROOT).endsWith(".properties")) {
            throw rejectedProperties("l'extension du fichier doit être .properties.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw rejectedProperties("le fichier n'a pas pu être lu correctement. Il est peut-être incomplet ou corrompu.");
        }

        String contenu = decodeUtf8Strict(bytes);

        Properties proprietes = new Properties();
        try {
            proprietes.load(new StringReader(contenu));
        } catch (IOException | IllegalArgumentException e) {
            throw rejectedProperties("le contenu n'a pas pu être interprété. Vérifiez la syntaxe cle=valeur et les caractères d'échappement.");
        }

        for (String cle : proprietes.stringPropertyNames()) {
            if (!clesAutorisees.contains(cle)) {
                throw rejectedProperties("la clé « " + cle + " » n'est pas reconnue par l'application. "
                        + "Partez du fichier proposé au téléchargement et n'en modifiez que les valeurs.");
            }
            String valeur = proprietes.getProperty(cle);
            if (valeur != null && (valeur.contains("<") || valeur.contains(">"))) {
                throw rejectedProperties("la valeur de la clé « " + cle + " » contient des chevrons. "
                        + "Les libellés doivent être en texte pur, sans balise HTML.");
            }
        }

        return new ValidatedProperties(bytes, proprietes);
    }

    private String decodeUtf8Strict(byte[] bytes) {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            throw rejectedProperties("le fichier n'est pas encodé en UTF-8. Réenregistrez-le en UTF-8 depuis votre éditeur de texte, "
                    + "sans quoi les caractères accentués seraient illisibles dans les documents imprimés.");
        }
    }

    private AppException rejectedProperties(String reason) {
        return new AppException(HttpStatus.BAD_REQUEST, "Fichier de libellés refusé : " + reason);
    }

    private void validatePdfSignature(byte[] bytes) {
        if (bytes.length < 8
                || bytes[0] != '%'
                || bytes[1] != 'P'
                || bytes[2] != 'D'
                || bytes[3] != 'F'
                || bytes[4] != '-') {
            throw rejectedPdf("la signature interne %PDF- est absente. Le fichier a probablement été renommé en .pdf sans être un vrai PDF.");
        }

        int trailerSize = Math.min(bytes.length, 2048);
        String trailer = new String(bytes, bytes.length - trailerSize, trailerSize, StandardCharsets.ISO_8859_1);
        if (!trailer.contains("%%EOF")) {
            throw rejectedPdf("le marqueur de fin %%EOF est absent. Le fichier semble incomplet, tronqué ou corrompu.");
        }
    }

    private void validatePdfContent(byte[] bytes) {
        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)))) {
            if (pdfDocument.getNumberOfPages() < 1) {
                throw rejectedPdf("le document ne contient aucune page exploitable.");
            }
            checkForbiddenStructures(pdfDocument);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw rejectedPdf(pdfParsingReason(e));
        }
    }

    /**
     * Inspection structurelle du PDF : parcourt le graphe d'objets accessible depuis le trailer
     * et rejette les constructions dangereuses (JavaScript, lancement de programme, fichiers
     * intégrés, médias riches, formulaires XFA).
     * Contrairement à une recherche de sous-chaînes sur les octets bruts, cette approche
     * ne produit pas de faux positifs sur les flux compressés (images, contenus) et détecte
     * les objets dangereux même stockés dans des flux d'objets compressés (/ObjStm).
     */
    private void checkForbiddenStructures(PdfDocument pdfDocument) {
        Deque<PdfObject> toVisit = new ArrayDeque<>();
        Set<PdfIndirectReference> visited = new HashSet<>();
        toVisit.push(pdfDocument.getTrailer());

        while (!toVisit.isEmpty()) {
            PdfObject current = toVisit.pop();
            if (current == null) {
                continue;
            }
            PdfIndirectReference reference = current.getIndirectReference();
            if (reference != null && !visited.add(reference)) {
                continue;
            }

            if (current instanceof PdfDictionary dictionary) {
                checkForbiddenDictionary(dictionary);
                for (PdfName key : dictionary.keySet()) {
                    toVisit.push(dictionary.get(key));
                }
            } else if (current instanceof PdfArray array) {
                for (int i = 0; i < array.size(); i++) {
                    toVisit.push(array.get(i));
                }
            }
        }
    }

    private void checkForbiddenDictionary(PdfDictionary dictionary) {
        PdfName actionType = dictionary.getAsName(PdfName.S);
        if (dictionary.containsKey(PdfName.JS)
                || dictionary.containsKey(PdfName.JavaScript)
                || PdfName.JavaScript.equals(actionType)) {
            throw rejectedPdf("le document contient du JavaScript, ce qui est interdit pour des raisons de sécurité.");
        }
        if (PdfName.Launch.equals(actionType)) {
            throw rejectedPdf("le document contient une action de lancement de programme, ce qui est interdit pour des raisons de sécurité.");
        }
        if (dictionary.containsKey(PdfName.EmbeddedFiles)
                || dictionary.containsKey(PdfName.EF)
                || PdfName.FileAttachment.equals(dictionary.getAsName(PdfName.Subtype))) {
            throw rejectedPdf("le document contient un fichier intégré, ce qui est interdit pour des raisons de sécurité.");
        }
        if (PdfName.RichMedia.equals(dictionary.getAsName(PdfName.Subtype))) {
            throw rejectedPdf("le document contient un média riche intégré, ce qui est interdit pour des raisons de sécurité.");
        }
        if (dictionary.containsKey(PdfName.XFA)) {
            throw rejectedPdf("le document contient un formulaire XFA, ce qui est interdit pour des raisons de sécurité.");
        }
    }

    private AppException rejectedPdf(String reason) {
        return new AppException(HttpStatus.BAD_REQUEST, "PDF refusé : " + reason);
    }

    private String pdfParsingReason(Exception e) {
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);
        if (message.contains("password") || message.contains("encrypted") || message.contains("decrypt")) {
            return "le document est protégé par mot de passe ou chiffré. Déposez une version PDF non protégée.";
        }
        if (message.contains("xref") || message.contains("trailer") || message.contains("eof")) {
            return "la structure interne du PDF est invalide. Le fichier est probablement corrompu ou mal exporté.";
        }
        return "la structure interne du PDF n'a pas pu être validée. Réexportez le document en PDF standard puis réessayez.";
    }

    private String readableContentType(String contentType) {
        return contentType == null || contentType.isBlank() ? "absent" : contentType;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024L) {
            return bytes + " o";
        }
        if (bytes < 1024L * 1024L) {
            return String.format(Locale.FRANCE, "%.1f Ko", bytes / 1024d);
        }
        return String.format(Locale.FRANCE, "%.2f Mo", bytes / (1024d * 1024d));
    }

    public record ValidatedImage(byte[] bytes, String contentType, String extension) {
    }

    public record ValidatedPdf(byte[] bytes, String contentType, String extension, String sha256) {
    }

    public record ValidatedProperties(byte[] bytes, Properties proprietes) {
    }

    private record ImageType(String contentType, String extension) {
        private static ImageType fromFormat(String formatName) {
            String normalized = formatName.toLowerCase(Locale.ROOT);
            return switch (normalized) {
                case "jpeg", "jpg" -> new ImageType("image/jpeg", "jpg");
                case "png" -> new ImageType("image/png", "png");
                case "gif" -> new ImageType("image/gif", "gif");
                case "bmp" -> new ImageType("image/bmp", "bmp");
                default -> new ImageType("image/" + normalized, normalized);
            };
        }
    }
}