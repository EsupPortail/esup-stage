package org.esup_portail.esup_stage.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class AppExceptionAdvice {

    private static final Logger LOGGER = LogManager.getLogger(AppExceptionAdvice.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Object> handleAppException(AppException e) {
        LOGGER.error(e.getMessage(), e);
        Map<String, Object> body = new HashMap<>();
        body.put("code", e.getHttpStatus());
        body.put("message", e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }
}
