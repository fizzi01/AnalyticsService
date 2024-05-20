package it.unisalento.pasproject.analyticsservice.exceptions;

import it.unisalento.pasproject.analyticsservice.exceptions.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class UserNotAuthorizedException extends CustomErrorException {

    public UserNotAuthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
