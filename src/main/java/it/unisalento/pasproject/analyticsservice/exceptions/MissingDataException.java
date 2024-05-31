package it.unisalento.pasproject.analyticsservice.exceptions;

import it.unisalento.pasproject.analyticsservice.exceptions.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class MissingDataException extends CustomErrorException {
    public MissingDataException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
