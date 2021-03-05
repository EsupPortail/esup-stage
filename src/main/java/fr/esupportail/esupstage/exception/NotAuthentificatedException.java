package fr.esupportail.esupstage.exception;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(UNAUTHORIZED)
public class NotAuthentificatedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
}
