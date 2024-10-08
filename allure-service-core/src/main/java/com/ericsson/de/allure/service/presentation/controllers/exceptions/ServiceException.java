package com.ericsson.de.allure.service.presentation.controllers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        reason = "Validation does not pass."
    )
public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}
