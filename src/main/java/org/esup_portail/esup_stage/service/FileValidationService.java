package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

@Service
public class FileValidationService {

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

    public record ValidatedImage(byte[] bytes, String contentType, String extension) {
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
