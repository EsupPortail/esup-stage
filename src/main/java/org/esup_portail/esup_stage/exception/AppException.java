package org.esup_portail.esup_stage.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

    private HttpStatus httpStatus;

    public AppException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
