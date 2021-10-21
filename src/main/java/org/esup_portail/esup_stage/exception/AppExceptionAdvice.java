package org.esup_portail.esup_stage.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AppExceptionAdvice {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Object> handleAppException(AppException e) {
        e.printStackTrace();
        return ResponseEntity.status(e.getHttpStatus()).body(e);
    }

}
