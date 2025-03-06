package org.esup_portail.esup_stage.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
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
}
