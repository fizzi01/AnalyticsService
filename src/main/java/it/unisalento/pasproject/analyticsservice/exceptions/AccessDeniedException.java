package it.unisalento.pasproject.analyticsservice.exceptions;

import it.unisalento.pasproject.analyticsservice.exceptions.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends CustomErrorException {
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
