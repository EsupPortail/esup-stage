package org.esup_portail.esup_stage.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
public class FilenameSanitizerService {
    private static final String DEFAULT_FILENAME = "document";
    private static final int MAX_FILENAME_LENGTH = 120;
    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{M}+");
    private static final Pattern UNSAFE_FILENAME_CHARS_PATTERN = Pattern.compile("[^A-Za-z0-9._-]+");
    private static final Pattern MULTIPLE_UNDERSCORES_PATTERN = Pattern.compile("_+");
    private static final Pattern EDGE_SEPARATORS_PATTERN = Pattern.compile("^[._-]+|[._-]+$");

    public String sanitize(String filename) {
        if (filename == null || filename.isBlank()) {
            return DEFAULT_FILENAME;
        }

        String normalized = Normalizer.normalize(filename, Normalizer.Form.NFD);
        normalized = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        normalized = normalized.replace(' ', '_');
        normalized = UNSAFE_FILENAME_CHARS_PATTERN.matcher(normalized).replaceAll("_");
        normalized = MULTIPLE_UNDERSCORES_PATTERN.matcher(normalized).replaceAll("_");
        normalized = EDGE_SEPARATORS_PATTERN.matcher(normalized).replaceAll("");

        if (normalized.isEmpty()) {
            return DEFAULT_FILENAME;
        }

        if (normalized.length() > MAX_FILENAME_LENGTH) {
            normalized = normalized.substring(0, MAX_FILENAME_LENGTH);
            normalized = EDGE_SEPARATORS_PATTERN.matcher(normalized).replaceAll("");
        }

        return normalized.isEmpty() ? DEFAULT_FILENAME : normalized;
    }

    public String sanitizeTempFilePrefix(String filename) {
        String sanitized = sanitize(filename);
        if (sanitized.length() >= 3) {
            return sanitized;
        }
        return (sanitized + "___").substring(0, 3);
    }
}