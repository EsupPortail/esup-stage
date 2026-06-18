package org.esup_portail.esup_stage.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {

    @ExceptionHandler({AppException.class, UsernameNotFoundException.class})
    public ResponseEntity<Object> handleAppException(AppException e) {
        log.error(e.getMessage(), e);
        Map<String, Object> body = new HashMap<>();
        body.put("code", e.getHttpStatus());
        body.put("message", e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<Object> handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        log.debug("Client disconnected during async response: {}", e.getMessage());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException e) {
        if (isClientDisconnectException(e)) {
            log.debug("Client disconnected during response write: {}", e.getMessage());
            return ResponseEntity.noContent().build();
        }

        log.error(e.getMessage(), e);
        Map<String, Object> body = new HashMap<>();
        body.put("code", HttpStatus.INTERNAL_SERVER_ERROR);
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private boolean isClientDisconnectException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String className = current.getClass().getName().toLowerCase(Locale.ROOT);
            if (className.contains("clientabort") || className.contains("asyncrequestnotusable")) {
                return true;
            }

            String message = current.getMessage();
            if (message != null) {
                String normalized = normalizeExceptionMessage(message);
                if (normalized.contains("broken pipe")
                        || normalized.contains("connection reset by peer")
                        || normalized.contains("forcibly closed by the remote host")
                        || normalized.contains("une connexion etablie a ete abandonnee")
                        || (normalized.contains("connexion") && normalized.contains("abandonn"))
                        || normalized.contains("an established connection was aborted")
                        || normalized.contains("servletoutputstream failed to flush")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private String normalizeExceptionMessage(String message) {
        String normalized = Normalizer.normalize(message, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT);
    }
}
