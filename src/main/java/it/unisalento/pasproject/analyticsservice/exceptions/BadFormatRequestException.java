package it.unisalento.pasproject.analyticsservice.exceptions;

import it.unisalento.pasproject.analyticsservice.exceptions.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class BadFormatRequestException extends CustomErrorException {
    public BadFormatRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
